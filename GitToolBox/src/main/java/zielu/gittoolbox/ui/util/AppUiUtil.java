package zielu.gittoolbox.ui.util;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Ref;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.DisposeSafeRunnable;

public final class AppUiUtil {
  private AppUiUtil() {
    //do nothing
  }

  private static Application getApplication() {
    return ApplicationManager.getApplication();
  }

  public static void invokeLater(@NotNull Runnable task) {
    Application application = getApplication();
    if (application.isUnitTestMode()) {
      task.run();
    } else {
      application.invokeLater(task);
    }
  }

  public static void invokeLater(@NotNull Project project, @NotNull Runnable task) {
    invokeLater(new DisposeSafeRunnable(project, task));
  }

  public static void invokeLaterIfNeeded(@NotNull Runnable task) {
    Application application = getApplication();
    if (application.isUnitTestMode() || application.isDispatchThread()) {
      task.run();
    } else {
      application.invokeLater(task);
    }
  }

  public static void invokeLaterIfNeeded(@NotNull Project project, @NotNull Runnable task) {
    invokeLaterIfNeeded(new DisposeSafeRunnable(project, task));
  }

  public static <T> T invokeAndWait(@NotNull Computable<T> task) {
    Application application = getApplication();
    if (application.isUnitTestMode()) {
      return task.compute();
    } else {
      Ref<T> value = new Ref<>();
      ApplicationManager.getApplication().invokeAndWait(() -> {
        value.set(task.compute());
      });
      return value.get();
    }
  }

  public static void invokeAndWait(@NotNull Runnable task) {
    Application application = getApplication();
    if (application.isUnitTestMode()) {
      task.run();
    } else {
      application.invokeAndWait(task);
    }
  }

  public static void invokeAndWait(@NotNull Project project, @NotNull Runnable task) {
    invokeAndWait(new DisposeSafeRunnable(project, task));
  }
}

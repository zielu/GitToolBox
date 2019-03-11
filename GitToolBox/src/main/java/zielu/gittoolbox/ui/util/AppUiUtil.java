package zielu.gittoolbox.ui.util;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Ref;

public final class AppUiUtil {
  private AppUiUtil() {
    //do nothing
  }

  private static Application getApplication() {
    return ApplicationManager.getApplication();
  }

  public static void invokeLater(Runnable task) {
    Application application = getApplication();
    if (application.isUnitTestMode()) {
      task.run();
    } else {
      application.invokeLater(task);
    }
  }

  public static void invokeLaterIfNeeded(Runnable task) {
    Application application = getApplication();
    if (application.isDispatchThread() || application.isUnitTestMode()) {
      task.run();
    } else {
      application.invokeLater(task);
    }
  }

  public static <T> T invokeAndWait(Computable<T> task) {
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
}

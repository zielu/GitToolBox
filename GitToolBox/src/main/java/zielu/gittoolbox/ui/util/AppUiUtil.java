package zielu.gittoolbox.ui.util;

import com.intellij.openapi.application.ApplicationManager;

public final class AppUiUtil {
  private AppUiUtil() {
    //do nothing
  }

  public static void invokeLater(Runnable task) {
    ApplicationManager.getApplication().invokeLater(task);
  }

  public static void invokeLaterIfNeeded(Runnable task) {
    invokeLater(task);
  }
}

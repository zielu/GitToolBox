package zielu.gittoolbox.ui.util;

import com.intellij.openapi.application.ApplicationManager;

public interface AppUtil {

  static void invokeLater(Runnable task) {
    ApplicationManager.getApplication().invokeLater(task);
  }

  static void invokeLaterIfNeeded(Runnable task) {
    invokeLater(task);
  }

}

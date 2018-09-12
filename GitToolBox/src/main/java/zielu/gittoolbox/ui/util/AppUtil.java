package zielu.gittoolbox.ui.util;

import com.intellij.openapi.application.ApplicationManager;

public interface AppUtil {
  AppUtil INSTANCE = task -> ApplicationManager.getApplication().invokeLater(task);

  void invokeLater(Runnable task);

  default void invokeLaterIfNeeded(Runnable task) {
    invokeLater(task);
  }
}

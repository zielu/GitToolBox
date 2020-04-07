package zielu.gittoolbox.config;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.Consumer;

public final class ConfigUtil {
  private static final Logger LOG = Logger.getInstance(ConfigUtil.class);

  private ConfigUtil() {
    //do nothing
  }

  public static void saveAppSettings(Consumer<GitToolBoxConfig2> modify) {
    Application application = ApplicationManager.getApplication();
    if (!application.isUnitTestMode()) {
      LOG.info("Saving settings");
      try {
        WriteAction.runAndWait(() -> {
          GitToolBoxConfig2 current = AppConfig.get();
          GitToolBoxConfig2 before = current.copy();
          modify.consume(current);
          application.saveSettings();
          current.fireChanged(before);
        });
      } catch (Exception exception) {
        LOG.error("Failed to save settings", exception);
      }
    }
  }
}

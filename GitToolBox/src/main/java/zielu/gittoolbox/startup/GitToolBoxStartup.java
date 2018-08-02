package zielu.gittoolbox.startup;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.config.GitToolBoxConfig2;

public class GitToolBoxStartup implements StartupActivity, DumbAware {
  private final Logger log = Logger.getInstance(getClass());
  private final Application application;

  public GitToolBoxStartup(Application application) {
    this.application = application;
  }

  @Override
  public void runActivity(@NotNull Project project) {
    migrateAppV1toV2();
    saveAppSettings();
  }

  private void migrateAppV1toV2() {
    GitToolBoxConfig2 v2 = GitToolBoxConfig2.getInstance();
    if (!v2.previousVersionMigrated) {
      GitToolBoxConfig v1 = GitToolBoxConfig.getInstance();
      ConfigMigratorV1toV2 migrator = new ConfigMigratorV1toV2();
      migrator.migrate(v1, v2);
      v2.previousVersionMigrated = true;
    }
  }

  private void saveAppSettings() {
    if (!application.isUnitTestMode()) {
      log.info("Saving settings");
      try {
        WriteAction.runAndWait((ThrowableRunnable<Exception>) application::saveSettings);
      } catch (Exception exception) {
        log.error("Failed to save settings", exception);
      }
    }
  }
}

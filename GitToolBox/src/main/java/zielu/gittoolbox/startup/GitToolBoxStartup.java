package zielu.gittoolbox.startup;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;

public class GitToolBoxStartup implements StartupActivity, DumbAware {
  private final Logger log = Logger.getInstance(getClass());

  public GitToolBoxStartup() {
  }

  @Override
  public void runActivity(@NotNull Project project) {
    boolean migrated = migrateAppV1toV2();
    migrated = migrateAppV2() || migrated;
    migrated = migrateProject(project) || migrated;
    if (migrated) {
      saveAppSettings();
    }
  }

  private boolean migrateAppV1toV2() {
    GitToolBoxConfig2 v2 = GitToolBoxConfig2.getInstance();
    if (!v2.previousVersionMigrated) {
      ConfigMigratorV1toV2 migrator = new ConfigMigratorV1toV2();
      migrator.migrate(v2);
      v2.previousVersionMigrated = true;
      log.info("V1 config migrated to V2");
      return true;
    }
    return false;
  }

  private boolean migrateAppV2() {
    GitToolBoxConfig2 v2 = GitToolBoxConfig2.getInstance();
    ConfigV2Migrator migrator = new ConfigV2Migrator(v2);
    boolean migrated = migrator.migrate();
    if (migrated) {
      log.info("V2 config migrated");
    }
    return migrated;
  }

  private boolean migrateProject(@NotNull Project project) {
    GitToolBoxConfigPrj config = GitToolBoxConfigPrj.getInstance(project);
    ConfigForProjectMigrator migrator = new ConfigForProjectMigrator(config);
    boolean migrated = migrator.migrate();
    if (migrated) {
      log.info("Project config migrated");
    }
    return migrated;
  }

  private void saveAppSettings() {
    Application application = ApplicationManager.getApplication();
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

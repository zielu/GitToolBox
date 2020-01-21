package zielu.gittoolbox.startup;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.config.GitToolBoxConfigExtras;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;

class ConfigMigrator {
  private final Logger log = Logger.getInstance(getClass());

  boolean migrate(@NotNull Project project) {
    GitToolBoxConfig2 v2 = GitToolBoxConfig2.getInstance();
    GitToolBoxConfigPrj prjConfig = GitToolBoxConfigPrj.getInstance(project);
    boolean migrated = migrateAppV1toV2(v2);
    migrated = migrateAppV2(v2) || migrated;
    migrated = migrateProject(prjConfig) || migrated;
    migrated = applyConfigOverrides(project, prjConfig) || migrated;

    return migrated;
  }

  private boolean migrateAppV1toV2(@NotNull GitToolBoxConfig2 v2) {
    if (!v2.getPreviousVersionMigrated()) {
      ConfigMigratorV1toV2 migrator = new ConfigMigratorV1toV2();
      migrator.migrate(v2);
      v2.setPreviousVersionMigrated(true);
      log.info("V1 config migrated to V2");
      return true;
    }
    return false;
  }

  private boolean migrateAppV2(@NotNull GitToolBoxConfig2 v2) {
    ConfigV2Migrator migrator = new ConfigV2Migrator(v2);
    boolean migrated = migrator.migrate();
    if (migrated) {
      log.info("V2 config migrated");
    }
    return migrated;
  }

  private boolean migrateProject(@NotNull GitToolBoxConfigPrj config) {
    ConfigForProjectMigrator migrator = new ConfigForProjectMigrator(config);
    boolean migrated = migrator.migrate();
    if (migrated) {
      log.info("Project config migrated");
    }
    return migrated;
  }

  private boolean applyConfigOverrides(@NotNull Project project, @NotNull GitToolBoxConfigPrj config) {
    if (project.isDefault()) {
      return false;
    }
    GitToolBoxConfigExtras override = GitToolBoxConfigExtras.getInstance();
    ConfigOverridesMigrator migrator = new ConfigOverridesMigrator(project, override);
    boolean migrated = migrator.migrate(config);
    if (migrated) {
      log.info("Project overrides applied");
    }
    return migrated;
  }
}

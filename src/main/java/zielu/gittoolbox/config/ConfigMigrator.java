package zielu.gittoolbox.config;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.store.WorkspaceState;
import zielu.gittoolbox.store.WorkspaceStore;

class ConfigMigrator {
  private final Logger log = Logger.getInstance(getClass());

  boolean migrate(GitToolBoxConfig2 appConfig) {
    boolean migrated = migrateAppV1toV2(appConfig);
    migrated = migrateV2(appConfig) || migrated;
    return migrated;
  }

  boolean migrate(@NotNull Project project,
                  @NotNull GitToolBoxConfig2 appConfig,
                  @NotNull GitToolBoxConfigPrj prjConfig) {
    WorkspaceState workspaceState = WorkspaceStore.get(project);
    boolean migrationPerformed = false;
    if (workspaceState.getProjectConfigVersion() == 1) {
      migrationPerformed = migrate(
          2,
          new ConfigForProjectMigrator1to2(prjConfig)::migrate,
          workspaceState::setProjectConfigVersion
      );
    }
    if (workspaceState.getProjectConfigVersion() == 2) {
      migrationPerformed = migrate(
          3,
          new ConfigForProjectMigrator2to3(appConfig, prjConfig)::migrate,
          workspaceState::setProjectConfigVersion
      );
    }
    return migrationPerformed;
  }

  private boolean migrate(int version, BooleanSupplier migrator, IntConsumer setVersion) {
    boolean migrated = migrator.getAsBoolean();
    if (migrated) {
      log.info("Project config migrated to version " + version);
    }
    setVersion.accept(version);
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

  private boolean migrateV2(@NotNull GitToolBoxConfig2 config) {
    boolean migrated = false;
    if (config.getVersion() == 1) {
      migrateV2(config, 2, ConfigMigratorV2.INSTANCE::migrate1To2);
      migrated = true;
    }
    if (config.getVersion() == 2) {
      migrateV2(config, 3, ConfigMigratorV2.INSTANCE::migrate2To3);
      migrated = true;
    }
    if (config.getVersion() == 3) {
      migrateV2(config, 4, ConfigMigratorV2.INSTANCE::migrate3To4);
      migrated = true;
    }
    return migrated;
  }

  private void migrateV2(@NotNull GitToolBoxConfig2 config, int version, Consumer<GitToolBoxConfig2> migrator) {
    migrator.accept(config);
    config.setVersion(version);
    log.info("V2 config migrated to version " + version);
  }
}

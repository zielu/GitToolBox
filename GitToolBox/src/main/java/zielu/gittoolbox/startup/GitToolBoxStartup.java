package zielu.gittoolbox.startup;

import static zielu.gittoolbox.config.DecorationPartType.BRANCH;
import static zielu.gittoolbox.config.DecorationPartType.LOCATION;
import static zielu.gittoolbox.config.DecorationPartType.STATUS;
import static zielu.gittoolbox.config.DecorationPartType.TAGS_ON_HEAD;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.ThrowableRunnable;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.DecorationPartConfig;
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
      migrate(v1, v2);
      v2.previousVersionMigrated = true;
    }
  }

  private void migrate(GitToolBoxConfig v1, GitToolBoxConfig2 v2) {
    v2.presentationMode = v1.presentationMode;
    v2.updateProjectActionId = v1.updateProjectActionId;
    v2.showStatusWidget = v1.showStatusWidget;
    v2.behindTracker = v1.behindTracker;
    v2.showProjectViewStatus = v1.showProjectViewStatus;

    List<DecorationPartConfig> decorationParts = new ArrayList<>();
    decorationParts.add(new DecorationPartConfig(BRANCH));
    decorationParts.add(new DecorationPartConfig(STATUS));
    if (v1.showProjectViewHeadTags) {
      DecorationPartConfig tagsOnHead = DecorationPartConfig.builder()
          .withType(TAGS_ON_HEAD)
          .withPrefix("(")
          .withPostfix(")")
          .build();
      decorationParts.add(tagsOnHead);
    }
    if (v1.showProjectViewLocationPath) {
      DecorationPartConfig.Builder location = DecorationPartConfig.builder().withType(LOCATION);
      if (v1.showProjectViewStatusBeforeLocation) {
        location.withPrefix("- ");
        decorationParts.add(location.build());
      } else {
        decorationParts.add(0, location.build());
      }
    }
    v2.decorationParts = decorationParts;
  }

  private void saveAppSettings() {
    if (!application.isUnitTestMode()) {
      log.info("Saving settings");

      try {
        WriteAction.runAndWait((ThrowableRunnable<Throwable>) application::saveSettings);
      } catch (Throwable throwable) {
        log.error("Failed to save settings", throwable);
      }
    }
  }
}

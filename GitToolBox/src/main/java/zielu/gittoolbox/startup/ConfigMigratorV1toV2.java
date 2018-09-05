package zielu.gittoolbox.startup;

import static zielu.gittoolbox.config.DecorationPartType.BRANCH;
import static zielu.gittoolbox.config.DecorationPartType.LOCATION;
import static zielu.gittoolbox.config.DecorationPartType.STATUS;
import static zielu.gittoolbox.config.DecorationPartType.TAGS_ON_HEAD;

import java.util.ArrayList;
import java.util.List;
import zielu.gittoolbox.config.DecorationPartConfig;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.config.GitToolBoxConfig2;

class ConfigMigratorV1toV2 {
  void migrate(GitToolBoxConfig v1, GitToolBoxConfig2 v2) {
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
}

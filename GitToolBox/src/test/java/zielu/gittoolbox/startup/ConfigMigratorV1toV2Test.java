package zielu.gittoolbox.startup;

import static org.assertj.core.api.Assertions.assertThat;
import static zielu.gittoolbox.config.DecorationPartType.BRANCH;
import static zielu.gittoolbox.config.DecorationPartType.LOCATION;
import static zielu.gittoolbox.config.DecorationPartType.STATUS;
import static zielu.gittoolbox.config.DecorationPartType.TAGS_ON_HEAD;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import zielu.gittoolbox.config.DecorationPartType;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.config.GitToolBoxConfig2;

@Tag("fast")
class ConfigMigratorV1toV2Test {
  private ConfigMigratorV1toV2 migrator = new ConfigMigratorV1toV2();

  @Test
  void shouldMigrateLocationBeforeStatus() {
    GitToolBoxConfig v1 = new GitToolBoxConfig();
    GitToolBoxConfig2 v2 = migrate(v1);

    assertDecorationParts(v2, LOCATION, BRANCH, STATUS, TAGS_ON_HEAD);
  }

  @Test
  void shouldMigrateStatusBeforeLocation() {
    GitToolBoxConfig v1 = new GitToolBoxConfig();
    v1.showProjectViewStatusBeforeLocation = true;
    GitToolBoxConfig2 v2 = migrate(v1);

    assertDecorationParts(v2, BRANCH, STATUS, TAGS_ON_HEAD, LOCATION);
  }

  @Test
  void shouldMigrateShowTagsOnHeadDisabled() {
    GitToolBoxConfig v1 = new GitToolBoxConfig();
    v1.showProjectViewHeadTags = false;
    GitToolBoxConfig2 v2 = migrate(v1);

    assertDecorationParts(v2, LOCATION, BRANCH, STATUS);
  }

  @Test
  void shouldMigrateShowLocationPathDisabled() {
    GitToolBoxConfig v1 = new GitToolBoxConfig();
    v1.showProjectViewLocationPath = false;
    GitToolBoxConfig2 v2 = migrate(v1);

    assertDecorationParts(v2, BRANCH, STATUS, TAGS_ON_HEAD);
  }

  private GitToolBoxConfig2 migrate(GitToolBoxConfig v1) {
    GitToolBoxConfig2 v2 = new GitToolBoxConfig2();
    migrator.migrate(v1, v2);
    return v2;
  }

  private void assertDecorationParts(GitToolBoxConfig2 v2, DecorationPartType... types) {
    assertThat(v2.decorationParts).extracting(part -> part.type).containsExactly(types);
  }
}
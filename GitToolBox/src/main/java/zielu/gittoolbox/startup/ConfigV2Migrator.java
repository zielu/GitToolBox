package zielu.gittoolbox.startup;

import zielu.gittoolbox.config.AuthorNameType;
import zielu.gittoolbox.config.GitToolBoxConfig2;

class ConfigV2Migrator {
  private final GitToolBoxConfig2 config;

  ConfigV2Migrator(GitToolBoxConfig2 config) {
    this.config = config;
  }

  boolean migrate() {
    if (config.blameInlineAuthorNameType == null) {
      config.blameInlineAuthorNameType = AuthorNameType.LASTNAME;
      return true;
    }
    return false;
  }
}

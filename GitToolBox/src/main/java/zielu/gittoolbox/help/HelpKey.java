package zielu.gittoolbox.help;

import java.util.Optional;
import zielu.gittoolbox.GitToolBox;

public enum HelpKey {
  APP_CONFIG(GitToolBox.PLUGIN_ID + ".appConfig", "global-configuration"),
  PROJECT_CONFIG(GitToolBox.PLUGIN_ID + ".projectConfig", "project-configuration")
  ;

  private final String id;
  private final String anchorId;

  HelpKey(String id, String anchorId) {
    this.id = id;
    this.anchorId = anchorId;
  }

  public String getId() {
    return id;
  }

  String getAnchorId() {
    return anchorId;
  }

  static Optional<HelpKey> findKeyById(String id) {
    for (HelpKey key : values()) {
      if (key.id.equals(id)) {
        return Optional.of(key);
      }
    }
    return Optional.empty();
  }
}

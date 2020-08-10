package zielu.gittoolbox.config;

import zielu.gittoolbox.ResBundle;

public enum DecorationPartType {
  BRANCH,
  STATUS,
  TAGS_ON_HEAD,
  CHANGED_COUNT,
  LOCATION,
  UNKNOWN
  ;

  public String getLabel() {
    return ResBundle.message("decoration.type." + name() + ".label");
  }

  public String getPlaceholder() {
    return ResBundle.message("decoration.type." + name() + ".placeholder");
  }
}

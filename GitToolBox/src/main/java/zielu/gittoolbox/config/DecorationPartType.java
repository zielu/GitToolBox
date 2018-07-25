package zielu.gittoolbox.config;

import zielu.gittoolbox.ResBundle;

public enum DecorationPartType {
  STATUS,
  TAGS_ON_HEAD,
  LOCATION
  ;

  public String getLabel() {
    return ResBundle.getString("decoration.type." + name() + ".label");
  }

  public String getPlaceholder() {
    return ResBundle.getString("decoration.type." + name() + ".placeholder");
  }
}

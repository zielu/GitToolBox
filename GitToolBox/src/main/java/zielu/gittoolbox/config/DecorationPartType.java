package zielu.gittoolbox.config;

import zielu.gittoolbox.ResBundle;

import java.util.EnumSet;
import java.util.Set;

public enum DecorationPartType {
  BRANCH,
  STATUS,
  TAGS_ON_HEAD,
  CHANGED_COUNT,
  LOCATION,
  UNKNOWN
  ;

  private static final EnumSet<DecorationPartType> VALUES;
  static {
    VALUES = EnumSet.complementOf(EnumSet.of(UNKNOWN));
  }

  public String getLabel() {
    return ResBundle.message("decoration.type." + name() + ".label");
  }

  public String getPlaceholder() {
    return ResBundle.message("decoration.type." + name() + ".placeholder");
  }

  public static Set<DecorationPartType> getValues() {
    return VALUES;
  }
}

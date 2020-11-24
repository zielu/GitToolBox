package zielu.gittoolbox.config;

import java.util.EnumSet;
import java.util.Set;
import zielu.gittoolbox.ResBundle;

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

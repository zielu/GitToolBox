package zielu.gittoolbox.config;

import java.util.Objects;

public class DecorationPartConfig {
  public DecorationPartType type;
  public String prefix = "";
  public String postfix = "";

  public DecorationPartConfig copy() {
    DecorationPartConfig copy = new DecorationPartConfig();
    copy.type = type;
    copy.prefix = prefix;
    copy.postfix = postfix;
    return copy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DecorationPartConfig that = (DecorationPartConfig) o;
    return type == that.type
        && Objects.equals(prefix, that.prefix)
        && Objects.equals(postfix, that.postfix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, prefix, postfix);
  }
}

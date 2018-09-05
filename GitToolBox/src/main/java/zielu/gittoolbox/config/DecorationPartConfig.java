package zielu.gittoolbox.config;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class DecorationPartConfig {
  public DecorationPartType type;
  public String prefix = "";
  public String postfix = "";

  public DecorationPartConfig() {
    //for serialization
  }

  public DecorationPartConfig(DecorationPartType type) {
    this.type = type;
  }

  public DecorationPartConfig copy() {
    DecorationPartConfig copy = new DecorationPartConfig(type);
    copy.prefix = prefix;
    copy.postfix = postfix;
    return copy;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private DecorationPartConfig config = new DecorationPartConfig();

    private Builder() {
    }

    public Builder withType(DecorationPartType type) {
      config.type = type;
      return this;
    }

    public Builder withPrefix(@NotNull String prefix) {
      config.prefix = prefix;
      return this;
    }

    public Builder withPostfix(@NotNull String postfix) {
      config.postfix = postfix;
      return this;
    }

    public DecorationPartConfig build() {
      return config;
    }
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

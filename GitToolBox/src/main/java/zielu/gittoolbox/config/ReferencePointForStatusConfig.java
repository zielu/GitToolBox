package zielu.gittoolbox.config;

import com.intellij.util.xmlb.annotations.Transient;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.Nullable;

public class ReferencePointForStatusConfig {
  public String type = ReferencePointForStatusType.TRACKED_REMOTE_BRANCH.key();
  public String name;

  @Transient
  public ReferencePointForStatusType getType() {
    return ReferencePointForStatusType.forKey(type);
  }

  @Transient
  public void setType(@Nullable ReferencePointForStatusType type) {
    if (type == null) {
      this.type = null;
    } else {
      this.type = type.key();
    }
  }

  public boolean isChanged(ReferencePointForStatusConfig config) {
    return !equals(config);
  }

  public ReferencePointForStatusConfig copy() {
    ReferencePointForStatusConfig copy = new ReferencePointForStatusConfig();
    copy.type = this.type;
    copy.name = this.name;
    return copy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    ReferencePointForStatusConfig that = (ReferencePointForStatusConfig) o;

    return new EqualsBuilder()
        .append(type, that.type)
        .append(name, that.name)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(type)
        .append(name)
        .toHashCode();
  }
}

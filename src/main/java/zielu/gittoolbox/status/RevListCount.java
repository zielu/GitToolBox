package zielu.gittoolbox.status;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.intellij.vcs.log.Hash;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;


public class RevListCount {
  private static final RevListCount CANCEL = new RevListCount(Status.CANCEL, null, null);
  private static final RevListCount FAILURE = new RevListCount(Status.FAILURE, null, null);
  private static final RevListCount NO_REMOTE = new RevListCount(Status.NO_REMOTE, null, null);

  private final Integer value;
  private final Hash top;
  private final Status status;

  private RevListCount(Status status, @Nullable Integer value, @Nullable Hash top) {
    this.value = value;
    this.status = status;
    this.top = top;
  }

  public static RevListCount success(int count, Hash top) {
    return new RevListCount(Status.SUCCESS, count, top);
  }

  public static RevListCount cancel() {
    return CANCEL;
  }

  public static RevListCount failure() {
    return FAILURE;
  }

  public static RevListCount noRemote() {
    return NO_REMOTE;
  }

  public int value() {
    Preconditions.checkState(Status.SUCCESS == status, "Value not possible for %s", status);
    return value;
  }

  @Nullable
  public Hash top() {
    return top;
  }

  public Status status() {
    return status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RevListCount that = (RevListCount) o;
    return new EqualsBuilder()
        .append(value, that.value)
        .append(top, that.top)
        .append(status, that.status)
        .build();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value, status);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
        .append("value", value)
        .append("status", status)
        .append("top", top)
        .build();
  }
}

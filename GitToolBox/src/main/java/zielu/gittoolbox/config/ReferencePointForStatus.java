package zielu.gittoolbox.config;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

public enum ReferencePointForStatus {
  TRACKED_REMOTE_BRANCH("trackedRemoteBranch"),
  SELECTED_PARENT_BRANCH("selectedParentBranch");

  private static final ImmutableMap<String, ReferencePointForStatus> VALUES;

  static {
    ImmutableMap.Builder<String, ReferencePointForStatus> builder = ImmutableMap.builder();
    for (ReferencePointForStatus value : ReferencePointForStatus.values()) {
      builder.put(value.key(), value);
    }
    VALUES = builder.build();
  }

  private final String key;

  ReferencePointForStatus(String key) {
    this.key = key;
  }

  public String key() {
    return key;
  }

  public static ReferencePointForStatus forKey(@NotNull String key) {
    return VALUES.get(key);
  }
}

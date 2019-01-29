package zielu.gittoolbox.config;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

public enum ReferencePointForStatusType {
  TRACKED_REMOTE_BRANCH("trackedRemoteBranch"),
  SELECTED_PARENT_BRANCH("selectedParentBranch");

  private static final ImmutableMap<String, ReferencePointForStatusType> VALUES;

  static {
    ImmutableMap.Builder<String, ReferencePointForStatusType> builder = ImmutableMap.builder();
    for (ReferencePointForStatusType value : ReferencePointForStatusType.values()) {
      builder.put(value.key(), value);
    }
    VALUES = builder.build();
  }

  private final String key;

  ReferencePointForStatusType(String key) {
    this.key = key;
  }

  public String key() {
    return key;
  }

  public static ReferencePointForStatusType forKey(@NotNull String key) {
    return VALUES.get(key);
  }
}

package zielu.gittoolbox.config;

import com.google.common.collect.ImmutableList;
import java.util.EnumSet;
import zielu.gittoolbox.ResBundle;

public enum ReferencePointForStatusType {
  AUTOMATIC("auto"),
  TRACKED_REMOTE_BRANCH("trackedRemoteBranch"),
  SELECTED_PARENT_BRANCH("selectedParentBranch")
  ;

  private static final ImmutableList<ReferencePointForStatusType> ALL_VALUES =
      ImmutableList.copyOf(EnumSet.allOf(ReferencePointForStatusType.class));

  private final String labelKey;

  ReferencePointForStatusType(String labelKey) {
    this.labelKey = "configurable.prj.parentBranch." + labelKey + ".label";
  }

  public String getLabel() {
    return ResBundle.message(labelKey);
  }

  public static ImmutableList<ReferencePointForStatusType> allValues() {
    return ALL_VALUES;
  }
}

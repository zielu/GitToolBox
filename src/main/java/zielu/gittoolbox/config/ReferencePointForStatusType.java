package zielu.gittoolbox.config;

import zielu.gittoolbox.ResBundle;

public enum ReferencePointForStatusType {
  AUTOMATIC("auto"),
  TRACKED_REMOTE_BRANCH("trackedRemoteBranch"),
  SELECTED_PARENT_BRANCH("selectedParentBranch")
  ;

  private final String labelKey;

  ReferencePointForStatusType(String labelKey) {
    this.labelKey = "configurable.prj.parentBranch." + labelKey + ".label";
  }

  public String getLabel() {
    return ResBundle.message(labelKey);
  }
}

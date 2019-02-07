package zielu.gittoolbox.ui;

import zielu.gittoolbox.status.BehindStatus;

public interface StatusPresenter {
  String behindStatus(BehindStatus behind);

  String aheadBehindStatus(int ahead, int behind);

  String nonZeroAheadBehindStatus(int ahead, int behind);

  String branchAndParent(String branchName, String parentBranchName);

  String key();

  String getLabel();
}

package zielu.gittoolbox.commit;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.checkin.CheckinHandler;

public class CommitMessageValidationCheckinHandler extends CheckinHandler {
  private final CheckinProjectPanel panel;
  private final String validationRegex;

  public CommitMessageValidationCheckinHandler(CheckinProjectPanel panel, String validationRegex) {
    this.panel = panel;
    this.validationRegex = validationRegex;
  }

  public ReturnResult beforeCheckin() {
    if (this.panel.getCommitMessage().matches(this.validationRegex)) {
      return ReturnResult.COMMIT;
    }
    new CommitMessageInvalidDialogWrapper().showAndGet();
    return ReturnResult.CANCEL;
  }
}

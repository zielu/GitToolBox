package zielu.gittoolbox.commit;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import javax.swing.JOptionPane;
import zielu.gittoolbox.ResBundle;

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
    int reply = JOptionPane.showConfirmDialog(this.panel.getComponent(),
        ResBundle.message("commit.message.validation.dialog.body"),
        ResBundle.message("commit.message.validation.dialog.title"),
        JOptionPane.YES_NO_OPTION);
    if (reply == JOptionPane.YES_OPTION) {
      return ReturnResult.COMMIT;
    }
    return ReturnResult.CANCEL;
  }
}

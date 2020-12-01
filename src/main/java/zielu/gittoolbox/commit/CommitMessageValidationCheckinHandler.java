package zielu.gittoolbox.commit;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.ui.BooleanCommitOption;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.compat.GitCompatUtil;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;
import zielu.gittoolbox.config.ProjectConfig;
import zielu.intellij.ui.YesNoDialog;

public class CommitMessageValidationCheckinHandler extends CheckinHandler {
  private final CheckinProjectPanel panel;

  public CommitMessageValidationCheckinHandler(CheckinProjectPanel panel) {
    this.panel = panel;
  }

  @Nullable
  @Override
  public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
    GitToolBoxConfigPrj config = getConfig();
    return new BooleanCommitOption(panel, ResBundle.message("commit.message.validation.label"),
        false, config::getCommitMessageValidation,
        config::setCommitMessageValidation);
  }

  private GitToolBoxConfigPrj getConfig() {
    return ProjectConfig.get(panel.getProject());
  }

  @Override
  public ReturnResult beforeCheckin() {
    if (shouldValidate()) {
      return validate();
    } else {
      return ReturnResult.COMMIT;
    }
  }

  private boolean shouldValidate() {
    return getConfig().getCommitMessageValidation() && hasModificationsUnderGit();
  }

  private boolean hasModificationsUnderGit() {
    return !GitCompatUtil.getRepositoriesForFiles(panel.getProject(), panel.getFiles()).isEmpty();
  }

  private ReturnResult validate() {
    if (this.panel.getCommitMessage().matches(getConfig().getCommitMessageValidationRegex())) {
      return ReturnResult.COMMIT;
    }
    YesNoDialog confirmationDialog = new YesNoDialog(panel.getProject(),
        panel.getPreferredFocusedComponent(),
        ResBundle.message("commit.message.validation.dialog.title"),
        ResBundle.message("commit.message.validation.dialog.body")
    );
    confirmationDialog.makeCancelDefault();
    boolean commit = confirmationDialog.showAndGet();
    if (commit) {
      return ReturnResult.COMMIT;
    }
    return ReturnResult.CANCEL;
  }
}

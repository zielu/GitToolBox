package zielu.gittoolbox.commit;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.VcsCheckinHandlerFactory;
import git4idea.GitVcs;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;

public class CommitMessageValidationCheckinHandlerFactory extends VcsCheckinHandlerFactory {

  public CommitMessageValidationCheckinHandlerFactory() {
    super(GitVcs.getKey());
  }

  @NotNull
  @Override
  protected CheckinHandler createVcsHandler(CheckinProjectPanel panel) {
    GitToolBoxConfigPrj config = GitToolBoxConfigPrj.getInstance(panel.getProject());
    if (config.getCommitMessageValidation()) {
      return new CommitMessageValidationCheckinHandler(panel, config.getCommitMessageValidationRegex());
    }
    return CheckinHandler.DUMMY;
  }
}
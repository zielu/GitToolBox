package zielu.gittoolbox.completion;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import java.io.File;
import java.util.Collection;
import java.util.List;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.util.diagnostics.PerfWatch;

public class CompletionCheckinHandler extends CheckinHandler {
  private final Logger log = Logger.getInstance(getClass());
  private final CheckinProjectPanel panel;

  public CompletionCheckinHandler(CheckinProjectPanel panel) {
    this.panel = panel;
    captureSelectedRepositories(this.panel);
  }

  @Override
  public void includedChangesChanged() {
    captureSelectedRepositories(panel);
  }

  private void captureSelectedRepositories(CheckinProjectPanel panel) {
    GitToolBoxConfigForProject config = GitToolBoxConfigForProject.getInstance(panel.getProject());
    if (config.commitDialogCompletion) {
      updateAffectedFiles(panel);
    }
  }

  private void updateAffectedFiles(CheckinProjectPanel panel) {
    PerfWatch getAffectedWatch = PerfWatch.createStarted("Get affected");
    Collection<File> affected = panel.getFiles();
    getAffectedWatch.finish();
    GitToolBoxCompletionProject.getInstance(panel.getProject()).updateAffected(affected);
  }

  @Override
  public void checkinSuccessful() {
    dispose();
  }

  @Override
  public void checkinFailed(List<VcsException> exception) {
    dispose();
  }

  private void dispose() {
    GitToolBoxCompletionProject.getInstance(panel.getProject()).clearAffected();
  }
}

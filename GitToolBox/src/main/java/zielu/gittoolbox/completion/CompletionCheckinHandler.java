package zielu.gittoolbox.completion;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import java.io.File;
import java.util.Collection;
import java.util.List;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.metrics.MetricsHost;

public class CompletionCheckinHandler extends CheckinHandler {
  private final CheckinProjectPanel panel;

  public CompletionCheckinHandler(CheckinProjectPanel panel) {
    this.panel = panel;
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
    Project project = panel.getProject();
    Collection<File> affected = MetricsHost.project(project)
        .timer("completion-get-affected").timeSupplier(panel::getFiles);
    GitToolBoxCompletionProject.getInstance(project).updateAffected(affected);
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

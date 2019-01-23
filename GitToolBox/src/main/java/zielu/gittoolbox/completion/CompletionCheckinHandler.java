package zielu.gittoolbox.completion;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.metrics.ProjectMetrics;

class CompletionCheckinHandler extends CheckinHandler implements CompletionScopeProvider {
  private final CheckinProjectPanel panel;

  CompletionCheckinHandler(CheckinProjectPanel panel) {
    this.panel = panel;
  }

  @Override
  public void includedChangesChanged() {
    //do nothing
  }

  @Override
  public void checkinSuccessful() {
    //do nothing
  }

  @Override
  public void checkinFailed(List<VcsException> exception) {
    //do nothing
  }

  @NotNull
  @Override
  public Collection<File> getAffectedFiles() {
    GitToolBoxConfigForProject config = GitToolBoxConfigForProject.getInstance(panel.getProject());
    if (config.commitDialogCompletion) {
      return pullCurrentAffectedFiles();
    }
    return Collections.emptyList();
  }

  private Collection<File> pullCurrentAffectedFiles() {
    Project project = panel.getProject();
    return ProjectMetrics.getInstance(project).timer("completion-get-affected")
        .timeSupplier(panel::getFiles);
  }
}

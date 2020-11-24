package zielu.gittoolbox.ui.statusbar.actions;

import com.google.common.base.Preconditions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import git4idea.GitVcs;
import git4idea.i18n.GitBundle;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.fetch.GtFetchUtil;
import zielu.gittoolbox.ui.util.AppUiUtil;
import zielu.gittoolbox.util.GtUtil;

public class FetchAction extends DumbAwareAction {
  private final GitRepository repository;

  public FetchAction(@NotNull GitRepository repository) {
    super(GitBundle.getString("fetch.action.name"));
    this.repository = repository;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = repository.getProject();
    AppUiUtil.invokeLaterIfNeeded(project, this::fetch);
  }

  private void fetch() {
    Project project = repository.getProject();
    GitVcs.runInBackground(new Task.Backgroundable(Preconditions.checkNotNull(project),
        ResBundle.message("message.fetching", GtUtil.name(repository))) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        indicator.checkCanceled();
        GtFetchUtil.fetch(repository).showNotificationIfFailed();
      }
    });
  }
}

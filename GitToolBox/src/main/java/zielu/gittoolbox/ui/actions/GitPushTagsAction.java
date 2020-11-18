package zielu.gittoolbox.ui.actions;

import static zielu.gittoolbox.push.GtPushResult.Type.ERROR;
import static zielu.gittoolbox.push.GtPushResult.Type.NOT_AUTHORIZED;
import static zielu.gittoolbox.push.GtPushResult.Type.REJECTED;

import com.google.common.base.Joiner;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsNotifier;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import git4idea.actions.GitRepositoryAction;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.push.GtPushResult;
import zielu.gittoolbox.push.GtPushResult.Type;
import zielu.gittoolbox.tag.GitTagsPusher;
import zielu.gittoolbox.tag.TagsPushSpec;
import zielu.gittoolbox.ui.GitPushTagsDialog;

public class GitPushTagsAction extends GitRepositoryAction {
  private final EnumMap<Type, BiConsumer<VcsNotifier, GtPushResult>> errorResultHandlers = new EnumMap<>(Type.class);

  public GitPushTagsAction() {
    errorResultHandlers.put(ERROR, (notifier, result) ->
        notifier.notifyError(
            "gittoolbox.push.failed",
            ResBundle.message("message.tags.push.failed"),
            result.getOutput())
    );
    errorResultHandlers.put(REJECTED, (notifier, result) ->
        notifier.notifyWeakError(
            "gittoolbox.push.rejected",
            ResBundle.message("message.tags.push.rejected", Joiner.on(" ").join(result.getRejectedBranches()))
        )
    );
    errorResultHandlers.put(NOT_AUTHORIZED, (notifier, result) ->
        notifier.notifyError("gittoolbox.push.no.auth", "Not authorized", result.getOutput())
    );
  }

  @Override
  protected boolean isEnabled(AnActionEvent e) {
    Project project = getEventProject(e);
    if (project != null) {
      VirtualFileRepoCache repoCache = VirtualFileRepoCache.getInstance(project);
      return super.isEnabled(e) && repoCache.hasAnyRepositories();
    }
    return false;
  }

  @NotNull
  @Override
  protected String getActionName() {
    return ResBundle.message("action.push.tags");
  }

  @Override
  protected void perform(@NotNull Project project, @NotNull List<VirtualFile> gitRoots,
                         @NotNull VirtualFile defaultRoot) {
    GitPushTagsDialog dialog = new GitPushTagsDialog(project, gitRoots, defaultRoot);
    dialog.show();
    if (dialog.isOK()) {
      final Optional<TagsPushSpec> pushSpec = dialog.getPushSpec();
      if (pushSpec.isPresent()) {
        Task.Backgroundable task = new Task.Backgroundable(project, ResBundle.message("message.pushing"), false) {
          @Override
          public void run(@NotNull ProgressIndicator indicator) {
            GtPushResult result = GitTagsPusher.create(getProject(), indicator).push(pushSpec.get());
            handleResult(getProject(), result);
          }
        };
        GitVcs.runInBackground(task);
      }
    }
  }

  private void handleResult(Project project, GtPushResult pushResult) {
    VcsNotifier vcsNotifier = VcsNotifier.getInstance(project);
    if (pushResult.getType() == Type.SUCCESS) {
      vcsNotifier.notifySuccess(
          "gittoolbox.push.success",
          ResBundle.message("message.tags.pushed.title"),
          ResBundle.message("message.tags.pushed")
      );
    } else {
      errorResultHandlers.getOrDefault(pushResult.getType(), (notifier, result) -> { /*do nothing*/ })
          .accept(vcsNotifier, pushResult);
    }
  }
}

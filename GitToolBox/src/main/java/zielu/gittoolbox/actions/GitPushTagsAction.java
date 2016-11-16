package zielu.gittoolbox.actions;

import com.google.common.base.Joiner;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsNotifier;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import git4idea.actions.GitRepositoryAction;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.push.GtPushResult;
import zielu.gittoolbox.push.GtPushResult.Type;
import zielu.gittoolbox.tag.GitTagsPusher;
import zielu.gittoolbox.tag.TagsPushSpec;
import zielu.gittoolbox.ui.GitPushTagsDialog;

public class GitPushTagsAction extends GitRepositoryAction {

    @NotNull
    @Override
    protected String getActionName() {
        return ResBundle.getString("action.push.tags");
    }

    @Override
    protected void perform(@NotNull Project project, @NotNull List<VirtualFile> gitRoots,
                           @NotNull VirtualFile defaultRoot) {
        GitPushTagsDialog dialog = new GitPushTagsDialog(project, gitRoots, defaultRoot);
        dialog.show();
        if (dialog.isOK()) {
            final Optional<TagsPushSpec> pushSpec = dialog.getPushSpec();
            if (pushSpec.isPresent()) {
                Task.Backgroundable task = new Task.Backgroundable(project, ResBundle.getString("message.pushing"), false) {
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

    private void handleResult(Project project, GtPushResult result) {
        if (result.getType() == Type.SUCCESS) {
            VcsNotifier.getInstance(project).notifySuccess(ResBundle.getString("message.tags.pushed"));
        } else if (EnumSet.of(Type.ERROR, Type.REJECTED, Type.NOT_AUTHORIZED).contains(result.getType())) {
            showError(project, result);
        }
    }

    private void showError(Project project, GtPushResult result) {
        VcsNotifier notifier = VcsNotifier.getInstance(project);
        switch (result.getType()) {
            case ERROR: {
                notifier.notifyError("Push failed", result.getOutput());
                break;
            }
            case REJECTED: {
                notifier.notifyWeakError("Push rejected: " + Joiner.on(" ").join(result.getRejectedBranches()));
                break;
            }
            case NOT_AUTHORIZED: {
                notifier.notifyError("Not authorized", result.getOutput());
                break;
            }
        }
    }
}

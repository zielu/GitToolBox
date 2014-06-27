package zielu.gittoolbox.actions;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsNotifier;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import git4idea.actions.GitRepositoryAction;
import git4idea.push.GitSimplePushResult;
import git4idea.push.GitSimplePushResult.Type;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
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
                           @NotNull VirtualFile defaultRoot, Set<VirtualFile> affectedRoots,
                           List<VcsException> exceptions) throws VcsException {
        GitPushTagsDialog dialog = new GitPushTagsDialog(project, gitRoots, defaultRoot);
        dialog.show();
        if (dialog.isOK()) {
            final Optional<TagsPushSpec> pushSpec = dialog.getPushSpec();
            if (pushSpec.isPresent()) {
                Task.Backgroundable task = new Task.Backgroundable(project, ResBundle.getString("message.pushing"), false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        GitSimplePushResult result = GitTagsPusher.create(getProject(), indicator).push(pushSpec.get());
                        handleResult(getProject(), result);
                    }
                };
                GitVcs.runInBackground(task);

            }
        }
    }

    private void handleResult(Project project, GitSimplePushResult result) {
        if (result.getType() == Type.SUCCESS) {
            VcsNotifier.getInstance(project).notifySuccess(ResBundle.getString("message.tags.pushed"));
        } else if (EnumSet.of(Type.ERROR, Type.REJECT, Type.NOT_AUTHORIZED).contains(result.getType())) {
            showError(project, result);
        }
    }

    private void showError(Project project, GitSimplePushResult result) {
        VcsNotifier notifier = VcsNotifier.getInstance(project);
        switch (result.getType()) {
            case ERROR: {
                notifier.notifyError("Push failed", result.getOutput());
                break;
            }
            case REJECT: {
                notifier.notifyWeakError("Push rejected: " +  Joiner.on(" ").join(result.getRejectedBranches()));
                break;
            }
            case NOT_AUTHORIZED: {
                notifier.notifyError("Not authorized", result.getOutput());
                break;
            }
        }
    }
}

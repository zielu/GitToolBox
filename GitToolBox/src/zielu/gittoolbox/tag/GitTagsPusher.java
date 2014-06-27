package zielu.gittoolbox.tag;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import git4idea.GitUtil;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.commands.GitLineHandlerListener;
import git4idea.commands.GitLineHandlerPasswordRequestAware;
import git4idea.commands.GitStandardProgressAnalyzer;
import git4idea.push.GitSimplePushResult;
import git4idea.repo.GitBranchTrackInfo;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.push.GitPushRejectedDetector;

public class GitTagsPusher {
    private final Project myProject;
    private final ProgressIndicator myProgress;
    private final Git myGit;

    private GitTagsPusher(Project project, ProgressIndicator progress) {
        myProject = project;
        myProgress = progress;
        myGit = ServiceManager.getService(Git.class);
    }

    public static GitTagsPusher create(@NotNull Project project, @NotNull ProgressIndicator progress) {
        return new GitTagsPusher(Preconditions.checkNotNull(project), Preconditions.checkNotNull(progress));
    }

    @NotNull
    public GitSimplePushResult push(@NotNull TagsPushSpec pushSpec) {
        Preconditions.checkNotNull(pushSpec);
        GitRepository repository = GitUtil.getRepositoryManager(myProject).getRepositoryForRoot(pushSpec.gitRoot());
        Optional<GitBranchTrackInfo> trackInfo = Optional.fromNullable(GitUtil.getTrackInfoForCurrentBranch(repository));
        if (trackInfo.isPresent()) {
            GitRemote remote = trackInfo.get().getRemote();
            Optional<String> url = Optional.fromNullable(remote.getFirstUrl());
            if (url.isPresent()) {
                return push(pushSpec, repository, remote, url.get());
            } else {
                return GitSimplePushResult.error(ResBundle.message("message.no.remote.url", remote.getName()));
            }
        } else {
            return GitSimplePushResult.error(ResBundle.getString("message.cannot.push.without.tracking"));
        }
    }

    private GitSimplePushResult push(final TagsPushSpec pushSpec, final GitRepository repository,
                                     final GitRemote remote, final String url) {
        final GitLineHandlerListener progressListener = GitStandardProgressAnalyzer.createListener(myProgress);
        final GitPushRejectedDetector rejectedDetector = new GitPushRejectedDetector();
        GitCommandResult result = myGit.runRemoteCommand(new Computable<GitLineHandler>() {
            @Override
            public GitLineHandler compute() {
                final GitLineHandlerPasswordRequestAware h = new GitLineHandlerPasswordRequestAware(repository.getProject(), repository.getRoot(),
                                                                                                       GitCommand.PUSH);
                h.setUrl(url);
                h.setSilent(false);
                h.setStdoutSuppressed(false);
                h.addLineListener(progressListener);
                h.addLineListener(rejectedDetector);
                h.addProgressParameter();
                h.addParameters(remote.getName());
                h.addParameters(pushSpec.specs());
                return h;
            }
        });
        if (rejectedDetector.rejected()) {
            return GitSimplePushResult.reject(rejectedDetector.getRejectedBranches());
        } else {
            return translate(result);
        }
    }

    private GitSimplePushResult translate(GitCommandResult result) {
        if (result.success()) {
            return GitSimplePushResult.success();
        } else if (result.cancelled()) {
            return GitSimplePushResult.cancel();
        } else {
            return GitSimplePushResult.error(result.getErrorOutputAsJoinedString());
        }
    }
}

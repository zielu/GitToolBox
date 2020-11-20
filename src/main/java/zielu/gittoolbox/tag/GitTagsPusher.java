package zielu.gittoolbox.tag;

import com.google.common.base.Preconditions;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import git4idea.GitUtil;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.commands.GitLineHandlerListener;
import git4idea.commands.GitStandardProgressAnalyzer;
import git4idea.repo.GitBranchTrackInfo;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.push.GitPushRejectedDetector;
import zielu.gittoolbox.push.GtPushResult;

public class GitTagsPusher {
  private final Project project;
  private final ProgressIndicator progressIndicator;

  private GitTagsPusher(Project project, ProgressIndicator progress) {
    this.project = project;
    progressIndicator = progress;
  }

  public static GitTagsPusher create(@NotNull Project project, @NotNull ProgressIndicator progress) {
    return new GitTagsPusher(Preconditions.checkNotNull(project), Preconditions.checkNotNull(progress));
  }

  @NotNull
  public GtPushResult push(@NotNull TagsPushSpec pushSpec) {
    Preconditions.checkNotNull(pushSpec);
    GitRepository repository = GitUtil.getRepositoryManager(project).getRepositoryForRoot(pushSpec.gitRoot());
    if (repository == null) {
      return GtPushResult.error("Path " + pushSpec.gitRoot().getPath() + " is not a Git root");
    }
    Optional<GitBranchTrackInfo> trackInfo = Optional.ofNullable(GitUtil.getTrackInfoForCurrentBranch(repository));
    if (trackInfo.isPresent()) {
      GitRemote remote = trackInfo.get().getRemote();
      Optional<String> url = Optional.ofNullable(remote.getFirstUrl());
      if (url.isPresent()) {
        return push(pushSpec, repository, remote, url.get());
      } else {
        return GtPushResult.error(ResBundle.message("message.no.remote.url", remote.getName()));
      }
    } else {
      return GtPushResult.error(ResBundle.message("message.cannot.push.without.tracking"));
    }
  }

  private GtPushResult push(final TagsPushSpec pushSpec, final GitRepository repository,
                            final GitRemote remote, final String url) {
    final GitLineHandlerListener progressListener = GitStandardProgressAnalyzer.createListener(progressIndicator);
    final GitPushRejectedDetector rejectedDetector = new GitPushRejectedDetector();
    GitCommandResult result = Git.getInstance().runCommand(() -> {
      final GitLineHandler h = new GitLineHandler(repository.getProject(), repository.getRoot(),
          GitCommand.PUSH);
      h.setUrl(url);
      h.setSilent(false);
      h.setStdoutSuppressed(false);
      h.addLineListener(progressListener);
      h.addLineListener(rejectedDetector);
      h.addParameters("--progress");
      h.addParameters(remote.getName());
      h.addParameters(pushSpec.specs());
      return h;
    });
    if (rejectedDetector.rejected()) {
      return GtPushResult.reject(rejectedDetector.getRejectedBranches());
    } else {
      return translate(result);
    }
  }

  private GtPushResult translate(GitCommandResult result) {
    if (result.success()) {
      return GtPushResult.success();
    } else if (result.cancelled()) {
      return GtPushResult.cancel();
    } else {
      return GtPushResult.error(result.getErrorOutputAsJoinedString());
    }
  }
}

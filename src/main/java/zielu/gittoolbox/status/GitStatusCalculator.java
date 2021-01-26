package zielu.gittoolbox.status;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.vcs.log.Hash;
import git4idea.GitLocalBranch;
import git4idea.GitUtil;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.repo.GitBranchTrackInfo;
import git4idea.repo.GitRepository;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GitStatusCalculator {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;

  private GitStatusCalculator(@NotNull Project project) {
    this.project = project;
  }

  @NotNull
  public static GitStatusCalculator create(@NotNull Project project) {
    return new GitStatusCalculator(project);
  }

  @NotNull
  public RevListCount behindStatus(GitRepository repository) {
    Optional<GitBranchTrackInfo> trackInfo = trackInfoForCurrentBranch(repository);
    if (trackInfo.isPresent()) {
      return behindStatus(trackInfo.get(), repository);
    }
    return RevListCount.noRemote();
  }

  private RevListCount behindStatus(GitBranchTrackInfo trackInfo, GitRepository repository) {
    String localName = trackInfo.getLocalBranch().getName();
    String remoteName = trackInfo.getRemoteBranch().getNameForLocalOperations();
    GitAheadBehindCount count = doRevListLeftRight(localName, remoteName, repository);
    return count.getBehind();
  }

  @NotNull
  public GitAheadBehindCount aheadBehindStatus(@NotNull GitRepository repository, @Nullable Hash localHash,
                                               @Nullable Hash remoteHash) {
    if (localHash != null && remoteHash != null) {
      return doRevListLeftRight(localHash.asString(), remoteHash.asString(), repository);
    } else {
      return GitAheadBehindCount.noRemote();
    }
  }

  private GitAheadBehindCount aheadBehindStatus(GitLocalBranch localBranch, GitBranchTrackInfo trackInfo,
                                                GitRepository repository) {
    String localName = localBranch.getName();
    String remoteName = trackInfo.getRemoteBranch().getNameForLocalOperations();
    return doRevListLeftRight(localName, remoteName, repository);
  }

  private Optional<GitBranchTrackInfo> trackInfoForCurrentBranch(GitRepository repository) {
    GitBranchTrackInfo trackInfo = GitUtil.getTrackInfoForCurrentBranch(repository);
    return Optional.ofNullable(trackInfo);
  }

  @NotNull
  private GitAheadBehindCount doRevListLeftRight(String localRef, String remoteRef, GitRepository repository) {
    GitLineHandler handler = prepareLineHandler(localRef, remoteRef, repository);
    return executeRevListCount(handler);
  }

  private GitLineHandler prepareLineHandler(String localRef, String remoteRef, GitRepository repository) {
    String branches = localRef + "..." + remoteRef;
    final GitLineHandler handler = new GitLineHandler(project, repository.getRoot(), GitCommand.REV_LIST);
    handler.addParameters(branches, "--left-right");
    log.debug("Prepared count with refs: '", branches, "'");
    return handler;
  }

  private GitAheadBehindCount executeRevListCount(GitLineHandler handler) {
    GitRevListLeftRightCounter counter = new GitRevListLeftRightCounter();
    handler.addLineListener(counter);
    GitCommandResult result = Git.getInstance().runCommandWithoutCollectingOutput(handler);
    if (result.success()) {
      return GitAheadBehindCount.success(counter.ahead(), counter.aheadTop(), counter.behind(), counter.behindTop());
    } else if (result.cancelled()) {
      return GitAheadBehindCount.cancel();
    } else {
      log.warn("Ahead/behind count failed:\n" + result.getErrorOutputAsJoinedString());
      return GitAheadBehindCount.failure();
    }
  }
}

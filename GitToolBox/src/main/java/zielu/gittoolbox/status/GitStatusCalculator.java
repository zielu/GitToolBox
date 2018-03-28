package zielu.gittoolbox.status;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.vcs.log.Hash;
import git4idea.GitLocalBranch;
import git4idea.GitUtil;
import git4idea.commands.GitCommand;
import git4idea.commands.GitLineHandler;
import git4idea.commands.GitTask;
import git4idea.commands.GitTaskResultHandlerAdapter;
import git4idea.repo.GitBranchTrackInfo;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GitStatusCalculator {
  private final Logger log = Logger.getInstance(getClass());

  private final Project project;
  private final ProgressIndicator indicator;

  private GitStatusCalculator(@NotNull Project project, @NotNull ProgressIndicator indicator) {
    this.project = Preconditions.checkNotNull(project);
    this.indicator = Preconditions.checkNotNull(indicator);
  }

  @NotNull
  public static GitStatusCalculator create(@NotNull Project project, @NotNull ProgressIndicator indicator) {
    return new GitStatusCalculator(project, indicator);
  }

  public static GitStatusCalculator create(@NotNull Project project) {
    return create(project, new EmptyProgressIndicator());
  }

  public Map<GitRepository, RevListCount> behindStatus(Collection<GitRepository> repositories) {
    Map<GitRepository, RevListCount> result = Maps.newLinkedHashMap();
    for (GitRepository repository : repositories) {
      result.put(repository, behindStatus(repository));
    }
    return result;
  }

  @NotNull
  public RevListCount behindStatus(GitRepository repository) {
    Optional<GitBranchTrackInfo> trackInfo = trackInfoForCurrentBranch(repository);
    if (trackInfo.isPresent()) {
      return behindStatus(repository.getCurrentBranch(), trackInfo.get(), repository);
    }
    return RevListCount.noRemote();
  }

  private RevListCount behindStatus(GitLocalBranch currentBranch, GitBranchTrackInfo trackInfo,
                                    GitRepository repository) {
    String localName = currentBranch.getName();
    String remoteName = trackInfo.getRemoteBranch().getNameForLocalOperations();
    GitAheadBehindCount count = doRevListLeftRight(localName, remoteName, repository);
    return count.behind;
  }

  @NotNull
  public GitAheadBehindCount aheadBehindStatus(@NotNull GitRepository repository) {
    Optional<GitBranchTrackInfo> trackInfo = trackInfoForCurrentBranch(repository);
    if (trackInfo.isPresent()) {
      return aheadBehindStatus(repository.getCurrentBranch(), trackInfo.get(), repository);
    }
    return GitAheadBehindCount.noRemote();
  }

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
    //TODO: GitTagCalculator, GitTagsPusher (especially second one) have examples how to do this.
    GitTask task = new GitTask(project, handler, "rev-list count");
    task.setProgressIndicator(indicator);
    AtomicReference<GitAheadBehindCount> result = new AtomicReference<GitAheadBehindCount>();
    task.execute(true, false, new GitTaskResultHandlerAdapter() {
      @Override
      protected void onSuccess() {
        result.set(GitAheadBehindCount.success(counter.ahead(), counter.aheadTop(), counter.behind(),
            counter.behindTop()));
      }

      @Override
      protected void onCancel() {
        result.set(GitAheadBehindCount.cancel());
      }

      @Override
      protected void onFailure() {
        result.set(GitAheadBehindCount.failure());
      }
    });
    return Preconditions.checkNotNull(result.get(), "Null rev list left right");
  }
}

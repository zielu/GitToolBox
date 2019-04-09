package zielu.gittoolbox.cache;

import static zielu.gittoolbox.cache.PerRepoInfoCache.CACHE_CHANGE;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.TaskManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.vcs.log.Hash;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.repo.GitBranchTrackInfo;
import git4idea.repo.GitRepoInfo;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.config.ReferencePointForStatusType;

class InfoCacheGateway {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;

  private final MessageBus messageBus;

  InfoCacheGateway(@NotNull Project project) {
    this.project = project;
    messageBus = project.getMessageBus();
  }

  void notifyEvicted(@NotNull Collection<GitRepository> repositories) {
    messageBus.syncPublisher(CACHE_CHANGE).evicted(repositories);
  }

  void notifyRepoChanged(@NotNull GitRepository repo, @NotNull RepoInfo previous, @NotNull RepoInfo current) {
    messageBus.syncPublisher(CACHE_CHANGE).stateChanged(previous, current, repo);
    log.debug("Published cache changed event: ", repo);
  }

  @NotNull
  RepoStatus createRepoStatus(@NotNull GitRepository repository) {
    Hash localHash = null;
    RepoStatusRemote remote = RepoStatusRemote.empty();

    GitLocalBranch localBranch = repository.getCurrentBranch();
    if (localBranch != null) {
      GitRepoInfo repoInfo = repository.getInfo();
      localHash = repoInfo.getLocalBranchesWithHashes().get(localBranch);
      remote = createRemoteStatus(repository, localBranch);
    }

    return RepoStatus.create(localBranch, localHash, remote);
  }

  private RepoStatusRemote createRemoteStatus(@NotNull GitRepository repository, @NotNull GitLocalBranch localBranch) {
    GitToolBoxConfigForProject config = GitToolBoxConfigForProject.getInstance(project);
    GitRemoteBranch trackedBranch = localBranch.findTrackedBranch(repository);
    GitRemoteBranch parentBranch = null;
    GitRepoInfo repoInfo = repository.getInfo();
    ReferencePointForStatusType type = config.referencePointForStatus.type;
    if (type == ReferencePointForStatusType.TRACKED_REMOTE_BRANCH) {
      parentBranch = trackedBranch;
    } else if (type == ReferencePointForStatusType.SELECTED_PARENT_BRANCH) {
      GitBranchTrackInfo trackInfo = repository.getBranchTrackInfo(config.referencePointForStatus.name);
      if (trackInfo != null) {
        parentBranch = trackInfo.getRemoteBranch();
      }
    } else if (type == ReferencePointForStatusType.AUTOMATIC) {
      parentBranch = getRemoteBranchFromActiveTask(repository).orElse(trackedBranch);
    }

    Hash parentHash = repoInfo.getRemoteBranchesWithHashes().get(parentBranch);
    return new RepoStatusRemote(trackedBranch, parentBranch, parentHash);
  }

  private Optional<GitRemoteBranch> getRemoteBranchFromActiveTask(@NotNull GitRepository repository) {
    TaskManager manager = TaskManager.getManager(project);
    if (manager == null) {
      return Optional.empty();
    }
    LocalTask activeTask = manager.getActiveTask();
    return activeTask.getBranches(true).stream()
        .filter(branchInfo -> Objects.equals(repository.getPresentableUrl(), branchInfo.repository))
        .findFirst()
        .map(branchInfo -> branchInfo.name)
        .map(repository::getBranchTrackInfo)
        .map(GitBranchTrackInfo::getRemoteBranch);
  }
}

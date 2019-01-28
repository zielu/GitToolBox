package zielu.gittoolbox.cache;

import static zielu.gittoolbox.cache.PerRepoInfoCache.CACHE_CHANGE;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.vcs.log.Hash;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.repo.GitRepoInfo;
import git4idea.repo.GitRepository;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

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

  void notifyRepoChanged(@NotNull GitRepository repo, @NotNull RepoInfo info) {
    messageBus.syncPublisher(CACHE_CHANGE).stateChanged(info, repo);
    log.debug("Published cache changed event: ", repo);
  }

  @NotNull
  RepoStatus createRepoStatus(@NotNull GitRepository repository) {
    Hash localHash = null;
    GitRemoteBranch remote = null;
    Hash remoteHash = null;

    GitLocalBranch localBranch = repository.getCurrentBranch();
    if (localBranch != null) {
      GitRepoInfo repoInfo = repository.getInfo();
      localHash = repoInfo.getLocalBranchesWithHashes().get(localBranch);
      remote = localBranch.findTrackedBranch(repository);
      remoteHash = repoInfo.getRemoteBranchesWithHashes().get(remote);
    }

    return RepoStatus.create(localBranch, localHash, remote, remoteHash);
  }
}

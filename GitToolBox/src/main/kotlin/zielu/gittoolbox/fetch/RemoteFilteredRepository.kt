package zielu.gittoolbox.fetch

import git4idea.repo.GitRemote
import git4idea.repo.GitRepository
import zielu.gittoolbox.config.AutoFetchExclusionConfig
import java.util.Objects

class RemoteFilteredRepository(
  val repo: GitRepository,
  val config: AutoFetchExclusionConfig
) : GitRepository by repo {

  override fun getRemotes(): Collection<GitRemote> {
    return repo.remotes.filter { !config.isRemoteExcluded(it.name) }
  }

  override fun toString(): String {
    return "${javaClass.simpleName}[$presentableUrl]"
  }

  override fun equals(other: Any?): Boolean {
    if (other is RemoteFilteredRepository) {
      return repo == other.repo && config == other.config
    }
    return false
  }

  override fun hashCode(): Int {
    return Objects.hash(repo, config)
  }
}

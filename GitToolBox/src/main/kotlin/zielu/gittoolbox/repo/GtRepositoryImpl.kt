package zielu.gittoolbox.repo

import git4idea.repo.GitRemote
import git4idea.repo.GitRepository
import zielu.gittoolbox.util.GtUtil

class GtRepositoryImpl(val repo: GitRepository) : GtRepository, GitRepository by repo {

  override fun findRemote(name: String): GitRemote? {
    return repo.remotes.firstOrNull { name == it.name }
  }

  override fun hasRemotes(): Boolean {
    return GtUtil.hasRemotes(repo)
  }

  override fun getName(): String {
    return GtUtil.name(repo)
  }

  override fun toString(): String {
    return "${javaClass.simpleName}[$presentableUrl]"
  }

  override fun equals(other: Any?): Boolean {
    if (other is GtRepositoryImpl) {
      return repo == other.repo
    }
    return false
  }

  override fun hashCode(): Int {
    return repo.hashCode()
  }
}

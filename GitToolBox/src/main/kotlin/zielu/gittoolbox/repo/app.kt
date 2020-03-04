@file:JvmName("RepoKt")

package zielu.gittoolbox.repo

import git4idea.GitLocalBranch
import git4idea.repo.GitRemote
import git4idea.repo.GitRepository
import zielu.gittoolbox.util.GtUtil

internal fun createGtRepository(repository: GitRepository): GtRepository {
  return GtRepositoryImpl(repository)
}

private class GtRepositoryImpl(val repo: GitRepository) : GtRepository, GitRepository by repo {
  override fun findRemote(name: String): GitRemote? {
    return repo.remotes.firstOrNull { name == it.name }
  }

  override fun findLocalBranch(name: String): GitLocalBranch? {
    return repo.branches.findLocalBranch(name)
  }

  override fun getRootUrl(): String {
    return repo.root.url
  }

  override fun hasRemotes(): Boolean = GtUtil.hasRemotes(repo)

  override fun getName(): String = GtUtil.name(repo)

  override fun toString(): String {
    return "${javaClass.simpleName}[$presentableUrl]"
  }

  override fun equals(other: Any?): Boolean {
    if (other is GtRepositoryImpl) {
      return repo == other.repo
    }
    return false
  }

  override fun hashCode() = repo.hashCode()
}

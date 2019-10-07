package zielu.gittoolbox.repo

import git4idea.repo.GitRemote
import git4idea.repo.GitRepository

class GtRepositoryImpl(val repo: GitRepository) : GtRepository, GitRepository by repo {

  override fun findRemote(name: String): GitRemote? {
    return repo.remotes.firstOrNull { name == it.name }
  }
}

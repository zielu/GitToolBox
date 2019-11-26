package zielu.gittoolbox.repo

import git4idea.repo.GitRemote
import git4idea.repo.GitRepository

internal interface GtRepository : GitRepository {
  fun findRemote(name: String): GitRemote?

  fun hasRemotes(): Boolean

  fun getName(): String
}

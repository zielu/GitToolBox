package zielu.gittoolbox.repo

import git4idea.repo.GitRemote
import git4idea.repo.GitRepository

interface GtRepository : GitRepository {
  fun findRemote(name: String): GitRemote?
}

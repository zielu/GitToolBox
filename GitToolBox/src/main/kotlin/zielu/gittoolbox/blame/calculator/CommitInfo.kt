package zielu.gittoolbox.blame.calculator

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import git4idea.GitRevisionNumber
import java.util.Date

internal class CommitInfo(
  val revisionNumber: VcsRevisionNumber
) {
  var authorName: String? = null
  var authorDate: Date? = null
  var summary: String? = null

  constructor(commitHash: String) : this(GitRevisionNumber(commitHash))

  fun setAuthorTime(authorTime: Long) {
    authorDate = Date(1000 * authorTime)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as CommitInfo

    if (revisionNumber != other.revisionNumber) return false

    return true
  }

  override fun hashCode(): Int {
    return revisionNumber.hashCode()
  }

  companion object {
    @JvmField
    val NULL: CommitInfo = CommitInfo((VcsRevisionNumber.NULL))
  }
}

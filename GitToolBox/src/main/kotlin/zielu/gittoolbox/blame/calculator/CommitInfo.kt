package zielu.gittoolbox.blame.calculator

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import git4idea.GitRevisionNumber
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

internal class CommitInfo(
  val revisionNumber: VcsRevisionNumber
) {
  var authorName: String? = null
  var authorEmail: String? = null
  var summary: String? = null
  private var authorTime: Instant? = null
  private var authorTimeOffset = ZoneOffset.UTC
  val authorDateTime: ZonedDateTime? by lazy {
    createAuthorDateTime()
  }

  constructor(commitHash: String) : this(GitRevisionNumber(commitHash))

  private fun createAuthorDateTime(): ZonedDateTime? {
    if (authorTime == null) {
      return null
    }
    return ZonedDateTime.ofInstant(authorTime, authorTimeOffset)
  }

  fun setAuthorTime(authorTime: Instant) {
    this.authorTime = authorTime
  }

  fun setAuthorTimeOffset(authorTimeOffset: ZoneOffset) {
    this.authorTimeOffset = authorTimeOffset
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

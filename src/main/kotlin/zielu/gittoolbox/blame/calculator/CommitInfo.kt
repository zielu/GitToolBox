package zielu.gittoolbox.blame.calculator

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import git4idea.GitRevisionNumber
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

internal class CommitInfo private constructor(
  val revisionNumber: VcsRevisionNumber,
  val authorName: String? = null,
  val authorEmail: String? = null,
  val authorDateTime: ZonedDateTime? = null,
  val summary: String? = null
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as CommitInfo

    if (revisionNumber != other.revisionNumber) return false

    return true
  }

  override fun hashCode(): Int = revisionNumber.hashCode()

  companion object {
    @JvmField
    val NULL: CommitInfo = CommitInfo(VcsRevisionNumber.NULL)
  }

  class Builder(val revisionNumber: VcsRevisionNumber) {
    var authorName: String? = null
      private set
    var authorEmail: String? = null
      private set
    var authorTime: Instant? = null
      private set
    var authorTimeOffset: ZoneId = ZoneOffset.UTC
      private set
    var summary: String? = null
      private set

    constructor(commitHash: String) : this(GitRevisionNumber(commitHash))

    fun authorName(authorName: String) = apply { this.authorName = authorName }

    fun authorEmail(authorEmail: String) = apply { this.authorEmail = authorEmail }

    fun authorTime(authorTime: Instant) = apply { this.authorTime = authorTime }

    fun authorTimeOffset(authorTimeOffset: ZoneId) = apply { this.authorTimeOffset = authorTimeOffset }

    fun summary(summary: String) = apply { this.summary = summary }

    private fun createAuthorDateTime(): ZonedDateTime? {
      if (authorTime != null) {
        return ZonedDateTime.ofInstant(authorTime, authorTimeOffset)
      }
      return null
    }

    fun build(): CommitInfo {
      val authorDateTime = createAuthorDateTime()
      return CommitInfo(revisionNumber, authorName, authorEmail, authorDateTime, summary)
    }
  }
}

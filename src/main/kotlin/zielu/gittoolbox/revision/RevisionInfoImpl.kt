package zielu.gittoolbox.revision

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import java.time.ZonedDateTime

internal data class RevisionInfoImpl(
  private val revisionNumber: VcsRevisionNumber,
  private val author: String,
  private val date: ZonedDateTime,
  private val subject: String?,
  private val authorEmail: String?
) : RevisionInfo {

  override fun getRevisionNumber() = revisionNumber
  override fun getAuthor() = author
  override fun getDate() = date
  override fun getSubject() = subject
  override fun isEmpty() = false
  override fun isNotEmpty() = true
  override fun getAuthorEmail() = authorEmail
}

package zielu.gittoolbox.revision

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import java.util.Date

internal data class RevisionInfoImpl(
  private val revisionNumber: VcsRevisionNumber,
  private val author: String,
  private val date: Date,
  private val subject: String?
) : RevisionInfo {

  override fun getRevisionNumber() = revisionNumber
  override fun getAuthor() = author
  override fun getDate() = date
  override fun getSubject() = subject
  override fun isEmpty() = false
  override fun isNotEmpty() = true
}

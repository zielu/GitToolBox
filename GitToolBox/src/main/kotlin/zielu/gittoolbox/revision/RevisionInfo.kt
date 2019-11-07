package zielu.gittoolbox.revision

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import java.time.ZonedDateTime

interface RevisionInfo {
  fun getRevisionNumber(): VcsRevisionNumber
  fun getAuthor(): String
  fun getSubject(): String?
  fun isEmpty(): Boolean
  fun isNotEmpty(): Boolean
  fun getAuthorEmail(): String?
  fun getDate(): ZonedDateTime

  companion object {
    @JvmField
    val NULL = object : RevisionInfo {
      override fun getRevisionNumber() = VcsRevisionNumber.NULL
      override fun getAuthor() = "EMPTY"
      override fun getAuthorEmail(): String? = null
      override fun getDate() = ZonedDateTime.now()
      override fun getSubject(): String? = null
      override fun isEmpty() = true
      override fun isNotEmpty() = false
    }
  }
}

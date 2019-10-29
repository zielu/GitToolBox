package zielu.gittoolbox.revision

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import java.util.Date

interface RevisionInfo {
  fun getRevisionNumber(): VcsRevisionNumber
  fun getAuthor(): String
  fun getDate(): Date
  fun getSubject(): String?
  fun isEmpty(): Boolean
  fun isNotEmpty(): Boolean

  companion object {
    @JvmField
    val NULL = object : RevisionInfo {
      override fun getRevisionNumber() = VcsRevisionNumber.NULL
      override fun getAuthor() = "EMPTY"
      override fun getDate() = Date(0)
      override fun getSubject(): String? = null
      override fun isEmpty() = true
      override fun isNotEmpty() = false
    }
  }
}

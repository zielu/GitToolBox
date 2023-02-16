package zielu.gittoolbox.revision

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.NullVirtualFile
import java.time.ZonedDateTime

internal interface RevisionDataProvider {
  val baseRevision: VcsRevisionNumber
  val file: VirtualFile
  val lineCount: Int

  fun getRevisionNumber(lineIndex: Int): VcsRevisionNumber

  fun getAuthorDateTime(lineIndex: Int): ZonedDateTime?

  fun getAuthor(lineIndex: Int): String?

  fun getAuthorEmail(lineIndex: Int): String?

  fun getSubject(lineIndex: Int): String?

  companion object {
    val empty = object : RevisionDataProvider {
      override val baseRevision: VcsRevisionNumber
        get() = VcsRevisionNumber.NULL
      override val file: VirtualFile
        get() = NullVirtualFile.INSTANCE
      override val lineCount: Int
        get() = 0

      override fun getRevisionNumber(lineIndex: Int): VcsRevisionNumber = VcsRevisionNumber.NULL

      override fun getAuthorDateTime(lineIndex: Int): ZonedDateTime? = null

      override fun getAuthor(lineIndex: Int): String? = null

      override fun getAuthorEmail(lineIndex: Int): String? = null

      override fun getSubject(lineIndex: Int): String? = null

      override fun toString(): String = "RevisionDataProvider[EMPTY]"
    }
  }
}

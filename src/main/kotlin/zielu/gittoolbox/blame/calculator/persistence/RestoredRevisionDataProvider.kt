package zielu.gittoolbox.blame.calculator.persistence

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.revision.RevisionDataProvider
import java.time.ZonedDateTime

internal class RestoredRevisionDataProvider(
  override val baseRevision: VcsRevisionNumber,
  override val file: VirtualFile,
  private val lines: List<LineData>,
) : RevisionDataProvider {
  override val lineCount: Int = lines.size

  override fun getAuthorDateTime(lineIndex: Int): ZonedDateTime? {
    return lines[lineIndex].authorDateTime
  }

  override fun getAuthor(lineIndex: Int): String? {
    return lines[lineIndex].author
  }

  override fun getAuthorEmail(lineIndex: Int): String? {
    return lines[lineIndex].authorEmail
  }

  override fun getSubject(lineIndex: Int): String? {
    return lines[lineIndex].subject
  }

  override fun getRevisionNumber(lineIndex: Int): VcsRevisionNumber {
    return lines[lineIndex].revision
  }
}

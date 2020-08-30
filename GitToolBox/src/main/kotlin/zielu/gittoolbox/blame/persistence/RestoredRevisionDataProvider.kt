package zielu.gittoolbox.blame.persistence

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.revision.RevisionDataProvider
import java.time.ZonedDateTime

internal class RestoredRevisionDataProvider(
  private val revision: VcsRevisionNumber,
  private val file: VirtualFile,
  private val lines: List<LineData>
) : RevisionDataProvider {
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

  override fun getBaseRevision(): VcsRevisionNumber = revision

  override fun getFile(): VirtualFile = file

  override fun getLineCount(): Int = lines.size
}

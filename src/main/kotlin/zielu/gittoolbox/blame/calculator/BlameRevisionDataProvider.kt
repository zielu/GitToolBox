package zielu.gittoolbox.blame.calculator

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.revision.RevisionDataProvider
import java.time.ZonedDateTime

internal class BlameRevisionDataProvider(
  private val lineInfos: List<CommitInfo>,
  private val file: VirtualFile,
  private val baseRevision: VcsRevisionNumber
) : RevisionDataProvider {

  override fun getFile(): VirtualFile = file

  override fun getBaseRevision(): VcsRevisionNumber = baseRevision

  override fun getLineCount(): Int = lineInfos.size

  override fun getSubject(lineIndex: Int): String? {
    return lineInfos[lineIndex].summary
  }

  override fun getAuthorDateTime(lineIndex: Int): ZonedDateTime? {
    return lineInfos[lineIndex].authorDateTime
  }

  override fun getAuthorEmail(lineIndex: Int): String? {
    return lineInfos[lineIndex].authorEmail
  }

  override fun getAuthor(lineIndex: Int): String? {
    return lineInfos[lineIndex].authorName
  }

  override fun getRevisionNumber(lineIndex: Int): VcsRevisionNumber {
    return lineInfos[lineIndex].revisionNumber
  }
}

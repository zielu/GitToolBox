package zielu.gittoolbox.blame.calculator

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.revision.RevisionDataProvider
import java.time.ZonedDateTime

internal class BlameRevisionDataProvider(
  private val lineInfos: List<CommitInfo>,
  override val file: VirtualFile,
  override val baseRevision: VcsRevisionNumber,
) : RevisionDataProvider {

  override val lineCount: Int = lineInfos.size

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

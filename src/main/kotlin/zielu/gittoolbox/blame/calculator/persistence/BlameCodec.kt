package zielu.gittoolbox.blame.calculator.persistence

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitRevisionNumber
import zielu.gittoolbox.revision.RevisionDataProvider
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

internal object BlameCodec {
  fun toPersistent(revisionData: RevisionDataProvider): FileBlameState {
    val blamesAtLines = mutableMapOf<VcsRevisionNumber, Int>()
    val lines = mutableListOf<LineState>()

    for (lineIndex in 0 until revisionData.lineCount) {
      val revision = revisionData.getRevisionNumber(lineIndex)
      val lineRef = blamesAtLines[revision]
      if (lineRef != null) {
        lines.add(LineState(lineRef))
      } else {
        val state = capture(revisionData, lineIndex)
        blamesAtLines[revision] = lineIndex
        lines.add(LineState(blame = state))
      }
    }
    return FileBlameState(
      revision = capture(revisionData.baseRevision),
      lines = lines
    )
  }

  private fun capture(data: RevisionDataProvider, lineIndex: Int): LineBlameState {
    return LineBlameState(
      capture(data.getRevisionNumber(lineIndex)),
      data.getAuthorDateTime(lineIndex)?.format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
      data.getAuthor(lineIndex),
      data.getAuthorEmail(lineIndex),
      data.getSubject(lineIndex)
    )
  }

  private fun capture(revision: VcsRevisionNumber): BlameRevisionState {
    val gitRevision = revision as GitRevisionNumber
    return BlameRevisionState(
      gitRevision.asString(),
      gitRevision.timestamp.time
    )
  }

  fun fromPersistent(file: VirtualFile, state: FileBlameState): RevisionDataProvider {
    val revision = restore(state.revision)
    val lines = mutableListOf<LineData>()

    state.lines.forEach {
      if (it.lineRef > -1) {
        lines.add(lines[it.lineRef])
      } else {
        lines.add(restore(it.blame!!))
      }
    }

    return RestoredRevisionDataProvider(
      revision,
      file,
      lines.toList()
    )
  }

  private fun restore(revision: BlameRevisionState): VcsRevisionNumber {
    return GitRevisionNumber(revision.version, Date(revision.timestamp))
  }

  private fun restore(state: LineBlameState): LineData {
    return LineData(
      restore(state.revision),
      state.authorDateTime?.let { ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME) },
      state.author,
      state.authorEmail,
      state.subject
    )
  }
}

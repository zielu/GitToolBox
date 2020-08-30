package zielu.gittoolbox.blame.persistence

internal data class FileBlameState(
  var accessTimestamp: Long = 0,
  var revision: BlameRevisionState = BlameRevisionState(),
  var lines: List<LineState> = listOf()
)

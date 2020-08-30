package zielu.gittoolbox.blame.persistence

internal data class LineState(
  var lineRef: Int = -1,
  var blame: LineBlameState? = null
)

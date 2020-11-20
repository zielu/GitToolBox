package zielu.gittoolbox.blame.calculator.persistence

internal data class LineState(
  var lineRef: Int = -1,
  var blame: LineBlameState? = null
)

package zielu.gittoolbox.blame.calculator.persistence

internal data class LineBlameState(
  var revision: BlameRevisionState = BlameRevisionState(),
  var authorDateTime: String? = null,
  var author: String? = null,
  var authorEmail: String? = null,
  var subject: String? = null
)

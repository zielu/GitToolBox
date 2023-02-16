package zielu.gittoolbox

internal class GitToolBoxException(
  message: String,
  cause: Throwable?
) : RuntimeException(
  message,
  cause
) {
  constructor(message: String) : this(message, null)
}

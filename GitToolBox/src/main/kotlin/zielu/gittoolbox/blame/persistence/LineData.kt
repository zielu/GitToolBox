package zielu.gittoolbox.blame.persistence

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import java.time.ZonedDateTime

internal data class LineData(
  val revision: VcsRevisionNumber,
  val authorDateTime: ZonedDateTime?,
  val author: String?,
  val authorEmail: String?,
  val subject: String?
)

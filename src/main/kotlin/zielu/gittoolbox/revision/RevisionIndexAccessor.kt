package zielu.gittoolbox.revision

import com.intellij.vcs.log.data.index.IndexDataGetter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

internal class RevisionIndexAccessor(
  private val getter: IndexDataGetter,
  private val commitIndex: Int
) {

  fun getFullMessage(): String? {
    return getter.getFullMessage(commitIndex)
  }

  fun getAuthorDateTime(): ZonedDateTime? {
    return getter.getAuthorTime(commitIndex)?.let {
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
    }
  }
}

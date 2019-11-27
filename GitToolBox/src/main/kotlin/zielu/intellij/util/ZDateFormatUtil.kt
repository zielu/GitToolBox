package zielu.intellij.util

import com.intellij.util.text.SyncDateFormat
import zielu.gittoolbox.ResBundle
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Date

internal object ZDateFormatUtil {

  fun formatPrettyDateTime(
    date: ZonedDateTime,
    now: ZonedDateTime,
    dateFormat: SyncDateFormat
  ): String {
    if (now < date) {
      return ResBundle.na()
    }
    val nowInstant = now.toInstant()
    val dateInstant = date.toInstant()
    val duration = Duration.between(dateInstant, nowInstant)
    return if (duration < HOUR_AND_MINUTE) {
      formatPrettyUntilOneHour(duration)
    } else {
      formatOverOneHour(dateInstant, nowInstant, dateFormat)
    }
  }

  private fun formatPrettyUntilOneHour(duration: Duration): String {
    return if (duration < MINUTE) {
      ZResBundle.message("date.format.minutes.ago", 0)
    } else ZResBundle.message("date.format.minutes.ago", duration.dividedBy(60).seconds)
  }

  private fun formatOverOneHour(date: Instant, now: Instant, dateFormat: SyncDateFormat): String {
    val nowDate = now.atZone(ZoneOffset.UTC).toLocalDate()
    val referenceDate = date.atZone(ZoneOffset.UTC).toLocalDate()
    return when {
      nowDate == referenceDate -> {
        ZResBundle.message("date.format.today")
      }
      nowDate.minusDays(1) == referenceDate -> {
        ZResBundle.message("date.format.yesterday")
      }
      else -> {
        dateFormat.format(Date.from(date))
      }
    }
  }
}

private val MINUTE = Duration.ofSeconds(60)
private val HOUR = Duration.of(1, ChronoUnit.HOURS)
private val HOUR_AND_MINUTE = HOUR.plusSeconds(1)

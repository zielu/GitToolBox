package zielu.intellij.util

import com.intellij.util.text.SyncDateFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Date

internal object ZDateFormatUtil {

  /**
   * Formats relative date-time in following way:
   * 1. under 1 minutes - moments ago
   * 2. 1 minutes to 1 hour - x minutes, one hour ago
   * 3. over 1 hour - today, yesterday, according to date format
   */
  fun formatPrettyDateTime(
    date: ZonedDateTime,
    now: ZonedDateTime,
    dateFormat: SyncDateFormat
  ): String {
    if (now < date) {
      return ZResBundle.na()
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

  fun formatBetweenDates(date1: ZonedDateTime, date2: ZonedDateTime): String {
    val duration = Duration.between(date1, date2)
    return if (duration.isNegative) {
      // TODO: should handle future - in a x minutes
      ZResBundle.na()
    } else {
      formatRelativePastDuration(duration)
    }
  }

  private fun formatRelativePastDuration(duration: Duration): String {
    return when {
      duration < MINUTE -> {
        ZResBundle.message("date.format.moments.ago")
      }
      duration < HOUR -> {
        ZResBundle.message("date.format.n.minutes.ago", duration.dividedBy(MINUTE))
      }
      duration < DAY -> {
        ZResBundle.message("date.format.n.hours.ago", duration.dividedBy(HOUR))
      }
      duration < WEEK -> {
        ZResBundle.message("date.format.n.days.ago", duration.dividedBy(DAY))
      }
      duration < MONTH -> {
        ZResBundle.message("date.format.n.weeks.ago", duration.dividedBy(WEEK))
      }
      duration < YEAR -> {
        ZResBundle.message("date.format.n.months.ago", duration.dividedBy(MONTH))
      }
      else -> {
        ZResBundle.message("date.format.n.years.ago", duration.dividedBy(YEAR))
      }
    }
  }
}

private val MINUTE = Duration.ofSeconds(60)
private val HOUR = Duration.of(1, ChronoUnit.HOURS)
private val HOUR_AND_MINUTE = HOUR.plusSeconds(1)
private val DAY = Duration.of(1, ChronoUnit.DAYS)
private val WEEK = Duration.of(7, ChronoUnit.DAYS)
private val MONTH = Duration.of(30, ChronoUnit.DAYS)
private val YEAR = Duration.of(365, ChronoUnit.DAYS)

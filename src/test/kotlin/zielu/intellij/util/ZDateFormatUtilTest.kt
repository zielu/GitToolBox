package zielu.intellij.util

import com.intellij.util.text.SyncDateFormat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Date

internal class ZDateFormatUtilTest {
  private val dateFormat = SyncDateFormat(SimpleDateFormat("dd-MM-yyyy HH:mm:ss"))
  private val now = ZonedDateTime.ofInstant(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneOffset.UTC)
  private val nowMinusTwoDaysFormatted = dateFormat.format(Date.from(now.minus(Duration.ofDays(2)).toInstant()))

  @Test
  fun `format moments ago`() {
    val date = now.minus(1, ChronoUnit.MILLIS)
    val text = ZDateFormatUtil.formatPrettyDateTime(date, now, dateFormat)
    assertThat(text).isEqualTo("Moments ago")
  }

  @Test
  fun `handle date after now`() {
    val date = now.plusNanos(1)
    val text = ZDateFormatUtil.formatPrettyDateTime(date, now, dateFormat)
    assertThat(text).isEqualTo("N/A")
  }

  @Test
  fun `format now minus two days`() {
    val date = now.minus(Duration.ofDays(2))
    val text = ZDateFormatUtil.formatPrettyDateTime(date, now, dateFormat)
    assertThat(text).isEqualTo(nowMinusTwoDaysFormatted)
  }

  @Test
  fun `format as absolute date-time`() {
    val date = now.minus(Duration.parse("P2DT1H"))
    val text = ZDateFormatUtil.formatPrettyDateTime(date, now, dateFormat)
    assertThat(text).isEqualTo(dateFormat.format(Date.from(date.toInstant())))
  }

  @Test
  fun `format returns correct time if PST zone`() {
    val pstZone = ZoneId.of("America/Los_Angeles")
    val now = ZonedDateTime.ofInstant(Instant.parse("2007-12-03T02:15:30.00Z"), pstZone)
    val date = now.minus(Duration.parse("PT2H50M"))
    val text = ZDateFormatUtil.formatPrettyDateTime(date, now, dateFormat)
    assertThat(text).isEqualTo("Yesterday")
  }

  @ParameterizedTest
  @CsvSource(
    "PT1M,A minute ago",
    "PT2M,2 minutes ago",
    "PT60M,1 hour ago"
  )
  fun `format minutes ago`(diffFromNow: Duration, expectedText: String) {
    val date = now.minus(diffFromNow)
    val text = ZDateFormatUtil.formatPrettyDateTime(date, now, dateFormat)
    assertThat(text).isEqualTo(expectedText)
  }

  @ParameterizedTest
  @CsvSource(
    "PT60M,1 hour ago",
    "PT61M,Today"
  )
  fun `format hours ago`(diffFromNow: Duration, expectedText: String) {
    val date = now.minus(diffFromNow)
    val text = ZDateFormatUtil.formatPrettyDateTime(date, now, dateFormat)
    assertThat(text).isEqualTo(expectedText)
  }

  @ParameterizedTest
  @CsvSource(
    "PT1H1M,Today",
    "PT1H2M,Today",
    "PT2H,Today",
    "PT2H50M,Today",
    "PT24H,Yesterday",
    "PT24H1M,Yesterday"
  )
  fun `format today`(diffFromNow: Duration, expectedText: String) {
    val date = now.minus(diffFromNow)
    val text = ZDateFormatUtil.formatPrettyDateTime(date, now, dateFormat)
    assertThat(text).isEqualTo(expectedText)
  }

  @ParameterizedTest
  @CsvSource(
    "P1D,Yesterday",
    "P1DT1M,Yesterday",
    "P1DT1H,Yesterday"
  )
  fun `format yesterday`(diffFromNow: Duration, expectedText: String) {
    val date = now.minus(diffFromNow)
    val text = ZDateFormatUtil.formatPrettyDateTime(date, now, dateFormat)
    assertThat(text).isEqualTo(expectedText)
  }

  @ParameterizedTest
  @ValueSource(
    strings = ["PT0S", "PT1S", "PT59S"]
  )
  fun `format relative between under minute`(diffFromNow: Duration) {
    val date = now.minus(diffFromNow)
    val text = ZDateFormatUtil.formatBetweenDates(date, now)
    assertThat(text).isEqualTo("moments ago")
  }

  @ParameterizedTest
  @CsvSource(
    "PT60S,one minute ago",
    "PT2M1S,2 minutes ago",
    "PT59M,59 minutes ago"
  )
  fun `format relative between under hour`(diffFromNow: Duration, expectedText: String) {
    val date = now.minus(diffFromNow)
    val text = ZDateFormatUtil.formatBetweenDates(date, now)
    assertThat(text).isEqualTo(expectedText)
  }

  @ParameterizedTest
  @CsvSource(
    "PT60M,one hour ago",
    "PT120M,2 hours ago",
    "PT23H59M,23 hours ago"
  )
  fun `format relative between under day`(diffFromNow: Duration, expectedText: String) {
    val date = now.minus(diffFromNow)
    val text = ZDateFormatUtil.formatBetweenDates(date, now)
    assertThat(text).isEqualTo(expectedText)
  }

  @ParameterizedTest
  @CsvSource(
    "PT24H,one day ago",
    "P2D,2 days ago",
    "P6DT23H59M,6 days ago"
  )
  fun `format relative between under week`(diffFromNow: Duration, expectedText: String) {
    val date = now.minus(diffFromNow)
    val text = ZDateFormatUtil.formatBetweenDates(date, now)
    assertThat(text).isEqualTo(expectedText)
  }

  @ParameterizedTest
  @CsvSource(
    "P7D,one week ago",
    "P14D,2 weeks ago",
    "P29DT23H59M,4 weeks ago"
  )
  fun `format relative between under month`(diffFromNow: Duration, expectedText: String) {
    val date = now.minus(diffFromNow)
    val text = ZDateFormatUtil.formatBetweenDates(date, now)
    assertThat(text).isEqualTo(expectedText)
  }

  @ParameterizedTest
  @CsvSource(
    "P30D,one month ago",
    "P60D,2 months ago",
    "P364DT23H59M,12 months ago"
  )
  fun `format relative between under year`(diffFromNow: Duration, expectedText: String) {
    val date = now.minus(diffFromNow)
    val text = ZDateFormatUtil.formatBetweenDates(date, now)
    assertThat(text).isEqualTo(expectedText)
  }

  @ParameterizedTest
  @CsvSource(
    "P366D,one year ago",
    "P731D,2 years ago"
  )
  fun `format relative between years`(diffFromNow: Duration, expectedText: String) {
    val date = now.minus(diffFromNow)
    val text = ZDateFormatUtil.formatBetweenDates(date, now)
    assertThat(text).isEqualTo(expectedText)
  }
}

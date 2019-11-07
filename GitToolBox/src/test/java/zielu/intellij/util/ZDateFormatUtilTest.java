package zielu.intellij.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.util.text.SyncDateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import zielu.TestType;

@Tag(TestType.FAST)
class ZDateFormatUtilTest {
  private static final long MINUTE_MILLIS = 60 * 1000;
  private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
  private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
  private static final long TWO_DAYS_MILLIS = 2 * DAY_MILLIS;
  private static final SyncDateFormat DATE_FORMAT = new SyncDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));

  private static final ZonedDateTime NOW = ZonedDateTime.ofInstant(Instant.parse("2007-12-03T10:15:30.00Z"),
      ZoneOffset.UTC);
  private static final String NOW_MINUS_TWO_DAYS_FORMATTED = DATE_FORMAT.format(
      Date.from(NOW.minus(TWO_DAYS_MILLIS, ChronoUnit.MILLIS).toInstant()));

  @Test
  void formatMomentsAgo() {
    ZonedDateTime date = NOW.minus(1, ChronoUnit.MILLIS);
    String text = ZDateFormatUtil.formatPrettyDateTime(date, NOW, DATE_FORMAT);
    assertThat(text).isEqualTo("Moments ago");
  }

  @Test
  void handleDateAfterNow() {
    ZonedDateTime date = NOW.plusNanos(1);
    String text = ZDateFormatUtil.formatPrettyDateTime(date, NOW, DATE_FORMAT);
    assertThat(text).isEqualTo("N/A");
  }

  @ParameterizedTest
  @CsvSource({
      MINUTE_MILLIS + ",A minute ago",
      (MINUTE_MILLIS * 2) + ",2 minutes ago",
      (MINUTE_MILLIS * 60) + ",1 hour ago"
  })
  void formatMinutesAgo(long diffFromNow, String expectedText) {
    ZonedDateTime date = NOW.minus(diffFromNow, ChronoUnit.MILLIS);
    String text = ZDateFormatUtil.formatPrettyDateTime(date, NOW, DATE_FORMAT);
    assertThat(text).isEqualTo(expectedText);
  }

  @ParameterizedTest
  @CsvSource({
      HOUR_MILLIS + ",1 hour ago",
      (HOUR_MILLIS + MINUTE_MILLIS) + ",Today"
  })
  void formatHoursAgo(long diffFromNow, String expectedText) {
    ZonedDateTime date = NOW.minus(diffFromNow, ChronoUnit.MILLIS);
    String text = ZDateFormatUtil.formatPrettyDateTime(date, NOW, DATE_FORMAT);
    assertThat(text).isEqualTo(expectedText);
  }

  @CsvSource({
      (HOUR_MILLIS + MINUTE_MILLIS) + ",Today",
      (HOUR_MILLIS + MINUTE_MILLIS * 2) + ",Today",
      (HOUR_MILLIS + MINUTE_MILLIS * 60) + ",Today",
      (HOUR_MILLIS + MINUTE_MILLIS * 110) + ",Today",
      (HOUR_MILLIS + MINUTE_MILLIS * 60 * 23) + ",Yesterday",
      (HOUR_MILLIS + MINUTE_MILLIS * 60 * 23) + MINUTE_MILLIS + ",Yesterday"
  })
  void formatToday(long diffFromNow, String expectedText) {
    ZonedDateTime date = NOW.minus(diffFromNow, ChronoUnit.MILLIS);
    String text = ZDateFormatUtil.formatPrettyDateTime(date, NOW, DATE_FORMAT);
    assertThat(text).isEqualTo(expectedText);
  }

  @ParameterizedTest
  @CsvSource({
      DAY_MILLIS + ",Yesterday",
      (DAY_MILLIS + MINUTE_MILLIS) + ",Yesterday",
      (DAY_MILLIS + HOUR_MILLIS) + ",Yesterday"
  })
  void formatYesterday(long diffFromNow, String expectedText) {
    ZonedDateTime date = NOW.minus(diffFromNow, ChronoUnit.MILLIS);
    String text = ZDateFormatUtil.formatPrettyDateTime(date, NOW, DATE_FORMAT);
    assertThat(text).isEqualTo(expectedText);
  }

  @Test
  void formatNowMinusTwoDays() {
    ZonedDateTime date = NOW.minus(TWO_DAYS_MILLIS, ChronoUnit.MILLIS);
    String text = ZDateFormatUtil.formatPrettyDateTime(date, NOW, DATE_FORMAT);
    assertThat(text).isEqualTo(NOW_MINUS_TWO_DAYS_FORMATTED);
  }

  @Test
  void formatAsAbsoluteDateTime() {
    ZonedDateTime date = NOW.minus(DAY_MILLIS * 2 + HOUR_MILLIS, ChronoUnit.MILLIS);
    String text = ZDateFormatUtil.formatPrettyDateTime(date, NOW, DATE_FORMAT);
    assertThat(text).isEqualTo(DATE_FORMAT.format(Date.from(date.toInstant())));
  }

  @Test
  void formatReturnsCorrectTimeForPstZoneTime() {
    ZoneId pstZone = ZoneId.of("America/Los_Angeles");
    ZonedDateTime now = ZonedDateTime.ofInstant(Instant.parse("2007-12-03T02:15:30.00Z"), pstZone);
    ZonedDateTime date = now.minus(HOUR_MILLIS * 2 + 50 * MINUTE_MILLIS, ChronoUnit.MILLIS);
    String text = ZDateFormatUtil.formatPrettyDateTime(date, now, DATE_FORMAT);
    assertThat(text).isEqualTo("Yesterday");
  }
}

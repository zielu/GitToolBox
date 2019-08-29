package zielu.intellij.util;

import com.google.common.base.Preconditions;
import com.intellij.util.text.SyncDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.jetbrains.annotations.NotNull;

public class ZDateFormatUtil {
  private static final Duration MINUTE = Duration.ofSeconds(60);
  private static final Duration HOUR = Duration.of(1, ChronoUnit.HOURS);
  private static final Duration HOUR_AND_MINUTE = HOUR.plusSeconds(1);

  private ZDateFormatUtil() {
    //do nothing
  }

  public static String formatPrettyDateTime(@NotNull Date date, @NotNull Date now, @NotNull SyncDateFormat dateFormat) {
    Preconditions.checkArgument(now.compareTo(date) >= 0, "Date is before now");
    Instant nowInstant = Instant.ofEpochMilli(now.getTime());
    Instant dateInstant = Instant.ofEpochMilli(date.getTime());
    Duration duration = Duration.between(dateInstant, nowInstant);
    if (duration.compareTo(HOUR_AND_MINUTE) < 0) {
      return formatPrettyUntilOneHour(duration);
    } else {
      ZoneId systemZone = ZoneId.systemDefault();
      LocalDate nowDate = nowInstant.atZone(systemZone).toLocalDate();
      LocalDate referenceDate = dateInstant.atZone(systemZone).toLocalDate();
      if (nowDate.equals(referenceDate)) {
        return ZResBundle.message("date.format.today");
      } else if (nowDate.minusDays(1).equals(referenceDate)) {
        return ZResBundle.message("date.format.yesterday");
      } else {
        return dateFormat.format(date);
      }
    }
  }

  private static String formatPrettyUntilOneHour(Duration duration) {
    if (duration.compareTo(MINUTE) < 0) {
      return ZResBundle.message("date.format.minutes.ago", 0);
    }
    return ZResBundle.message("date.format.minutes.ago", duration.dividedBy(MINUTE.getSeconds()).getSeconds());
  }
}

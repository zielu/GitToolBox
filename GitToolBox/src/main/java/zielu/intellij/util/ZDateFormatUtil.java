package zielu.intellij.util;

import com.google.common.base.Preconditions;
import com.intellij.util.text.SyncDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.jetbrains.annotations.NotNull;

public class ZDateFormatUtil {
  private static final Duration MINUTE = Duration.ofSeconds(60);
  private static final Duration HOUR = Duration.of(1, ChronoUnit.HOURS);
  private static final Duration HOUR_AND_MINUTE = HOUR.plusSeconds(1);
  private static final Duration DAY = Duration.of(1, ChronoUnit.DAYS);
  private static final Duration TWO_DAYS = Duration.of(2, ChronoUnit.DAYS);

  private ZDateFormatUtil() {
    throw new IllegalStateException();
  }

  public static String formatPrettyDateTime(@NotNull Date date, @NotNull Date now, @NotNull SyncDateFormat dateFormat) {
    Preconditions.checkArgument(now.compareTo(date) >= 0, "Date is before now");
    Duration duration = Duration.ofMillis(now.getTime() - date.getTime());
    if (duration.compareTo(HOUR_AND_MINUTE) < 0) {
      return formatPrettyUntilOneHour(duration);
    } else if (duration.compareTo(DAY) <= 0) {
      return ZResBundle.message("date.format.today");
    } else if (duration.compareTo(TWO_DAYS) <= 0) {
      return ZResBundle.message("date.format.yesterday");
    } else {
      return dateFormat.format(date);
    }
  }

  private static String formatPrettyUntilOneHour(Duration duration) {
    if (duration.compareTo(MINUTE) < 0) {
      return ZResBundle.message("date.format.minutes.ago", 0);
    }
    return ZResBundle.message("date.format.minutes.ago", duration.dividedBy(MINUTE.getSeconds()).getSeconds());
  }
}

package zielu.gittoolbox.util;

import com.intellij.util.text.DateFormatUtil;
import java.util.Date;
import org.jetbrains.annotations.NotNull;

public final class DateFormattingUtil {
  private DateFormattingUtil() {
    throw new IllegalStateException();
  }

  @NotNull
  public static String formatPrettyDateTime(@NotNull Date date) {
    return DateFormatUtil.formatPrettyDateTime(date);
  }

  @NotNull
  public static String formatRelativeBetweenDateTimes(@NotNull Date start, @NotNull Date end) {
    return DateFormatUtil.formatBetweenDates(start.getTime(), end.getTime());
  }

  @NotNull
  public static String formatAbsoluteDateTime(@NotNull Date date) {
    return DateFormatUtil.formatDateTime(date);
  }
}

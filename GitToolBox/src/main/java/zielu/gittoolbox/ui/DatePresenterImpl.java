package zielu.gittoolbox.ui;

import com.intellij.util.text.SyncDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.config.DateType;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.util.DateFormattingUtil;
import zielu.intellij.util.ZDateFormatUtil;

class DatePresenterImpl implements DatePresenter {
  @Nullable
  @Override
  public String format(@NotNull DateType type, @Nullable ZonedDateTime date) {
    if (date == null) {
      return null;
    } else {
      return formatImpl(type, date);
    }
  }

  private String formatImpl(DateType type, ZonedDateTime date) {
    switch (type) {
      case AUTO: return formatPrettyDate(date);
      case ABSOLUTE: return formatAbsoluteDate(date);
      case RELATIVE: return DateFormattingUtil.formatRelativeBetweenDateTimes(Date.from(date.toInstant()), new Date());
      default: return "Unknown type " + type;
    }
  }

  private String formatPrettyDate(ZonedDateTime date) {
    return ZDateFormatUtil.formatPrettyDateTime(date, ZonedDateTime.now(), getAbsoluteDateTimeFormat());
  }

  private SyncDateFormat getAbsoluteDateTimeFormat() {
    return GitToolBoxConfig2.getInstance().absoluteDateTimeStyle.getFormat();
  }

  private String formatAbsoluteDate(ZonedDateTime date) {
    return getAbsoluteDateTimeFormat().format(Date.from(date.toInstant()));
  }
}

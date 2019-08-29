package zielu.gittoolbox.ui;

import com.intellij.util.text.SyncDateFormat;
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
  public String format(@NotNull DateType type, @Nullable Date date) {
    if (date == null) {
      return null;
    } else {
      return formatImpl(type, date);
    }
  }

  private String formatImpl(DateType type, Date date) {
    switch (type) {
      case AUTO: return formatPrettyDate(date);
      case ABSOLUTE: return formatAbsoluteDate(date);
      case RELATIVE: return DateFormattingUtil.formatRelativeBetweenDateTimes(date, new Date());
      default: return "Unknown type " + type;
    }
  }

  private String formatPrettyDate(Date date) {
    return ZDateFormatUtil.formatPrettyDateTime(date, new Date(), getAbsoluteDateTimeFormat());
  }

  private SyncDateFormat getAbsoluteDateTimeFormat() {
    return GitToolBoxConfig2.getInstance().absoluteDateTimeStyle.getFormat();
  }

  private String formatAbsoluteDate(Date date) {
    return getAbsoluteDateTimeFormat().format(date);
  }
}

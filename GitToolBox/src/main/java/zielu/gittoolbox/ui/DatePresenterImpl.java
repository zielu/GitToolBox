package zielu.gittoolbox.ui;

import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.config.DateType;
import zielu.gittoolbox.util.DateFormattingUtil;

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
      case AUTO: return DateFormattingUtil.formatPrettyDateTime(date);
      case ABSOLUTE: return DateFormattingUtil.formatAbsoluteDateTime(date);
      case RELATIVE: return DateFormattingUtil.formatRelativeBetweenDateTimes(date, new Date());
      default: return "";
    }
  }
}

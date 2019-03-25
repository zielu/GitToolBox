package zielu.gittoolbox.config;

import com.intellij.util.text.DateFormatUtil;
import com.intellij.util.xmlb.annotations.Transient;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;

public enum DateType {
  AUTO("date.type.auto") {
    @NotNull
    @Override
    protected String formatImpl(@NotNull Date date) {
      return DateFormatUtil.formatPrettyDateTime(date);
    }
  },
  RELATIVE("date.type.relative") {
    @NotNull
    @Override
    protected String formatImpl(@NotNull Date date) {
      return DateFormatUtil.formatBetweenDates(date.getTime(), System.currentTimeMillis());
    }
  },
  ABSOLUTE("date.type.absolute") {
    @NotNull
    @Override
    protected String formatImpl(@NotNull Date date) {
      return DateFormatUtil.formatDateTime(date);
    }
  };

  private final String descriptionKey;

  DateType(String descriptionKey) {
    this.descriptionKey = descriptionKey;
  }

  @Transient
  public String getDescription() {
    return ResBundle.message(descriptionKey);
  }

  @Nullable
  public String format(@Nullable Date dateTime) {
    if (dateTime != null) {
      return formatImpl(dateTime);
    } else {
      return null;
    }
  }

  @NotNull
  protected abstract String formatImpl(@NotNull Date dateTime);
}

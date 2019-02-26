package zielu.gittoolbox.config;

import com.intellij.util.text.DateFormatUtil;
import com.intellij.util.xmlb.annotations.Transient;
import java.time.LocalDate;
import java.time.ZoneId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;

public enum DateType {
  AUTO("date.type.auto") {
    @NotNull
    @Override
    protected String formatImpl(@NotNull LocalDate date) {
      return DateFormatUtil.formatPrettyDate(toTimestamp(date));
    }
  },
  RELATIVE("date.type.relative") {
    @NotNull
    @Override
    protected String formatImpl(@NotNull LocalDate date) {
      long timestamp = toTimestamp(date);
      return DateFormatUtil.formatBetweenDates(timestamp, System.currentTimeMillis());
    }
  },
  ABSOLUTE("date.type.absolute") {
    @NotNull
    @Override
    protected String formatImpl(@NotNull LocalDate date) {
      return DateFormatUtil.formatDate(toTimestamp(date));
    }
  };

  private final String descriptionKey;

  DateType(String descriptionKey) {
    this.descriptionKey = descriptionKey;
  }

  @Transient
  public String getDescription() {
    return ResBundle.getString(descriptionKey);
  }

  @Nullable
  public String format(@Nullable LocalDate date) {
    if (date != null) {
      return formatImpl(date);
    } else {
      return null;
    }
  }

  @NotNull
  protected abstract String formatImpl(@NotNull LocalDate date);

  protected final long toTimestamp(@NotNull LocalDate date) {
    return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }
}

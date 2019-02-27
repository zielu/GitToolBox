package zielu.gittoolbox.config;

import com.intellij.util.text.DateFormatUtil;
import com.intellij.util.xmlb.annotations.Transient;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;

public enum DateType {
  AUTO("date.type.auto") {
    @NotNull
    @Override
    protected String formatImpl(@NotNull LocalDateTime date) {
      return DateFormatUtil.formatPrettyDateTime(toTimestamp(date));
    }
  },
  RELATIVE("date.type.relative") {
    @NotNull
    @Override
    protected String formatImpl(@NotNull LocalDateTime date) {
      long timestamp = toTimestamp(date);
      return DateFormatUtil.formatBetweenDates(timestamp, System.currentTimeMillis());
    }
  },
  ABSOLUTE("date.type.absolute") {
    @NotNull
    @Override
    protected String formatImpl(@NotNull LocalDateTime date) {
      return DateFormatUtil.formatDateTime(toTimestamp(date));
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
  public String format(@Nullable LocalDateTime dateTime) {
    if (dateTime != null) {
      return formatImpl(dateTime);
    } else {
      return null;
    }
  }

  @NotNull
  protected abstract String formatImpl(@NotNull LocalDateTime dateTime);

  protected final long toTimestamp(@NotNull LocalDateTime date) {
    return date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }
}

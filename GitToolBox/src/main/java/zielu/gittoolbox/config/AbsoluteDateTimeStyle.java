package zielu.gittoolbox.config;

import com.intellij.util.text.DateFormatUtil;
import com.intellij.util.text.SyncDateFormat;
import com.intellij.util.xmlb.annotations.Transient;
import java.text.SimpleDateFormat;
import zielu.gittoolbox.ResBundle;

public enum AbsoluteDateTimeStyle {
  FROM_LOCALE(0) {
    @Transient
    @Override
    public SyncDateFormat getFormat() {
      return DateFormatUtil.getDateTimeFormat();
    }
  },
  DMY_TIME(1) {
    private final transient SyncDateFormat format = new SyncDateFormat(
        new SimpleDateFormat("dd MMM yyyy, HH:mm"));

    @Transient
    @Override
    public SyncDateFormat getFormat() {
      return format;
    }
  },
  YMD_TIME(2) {
    private final transient SyncDateFormat format = new SyncDateFormat(
        new SimpleDateFormat("yyyy MMM dd, HH:mm"));

    @Transient
    @Override
    public SyncDateFormat getFormat() {
      return format;
    }
  },
  MDY_TIME(3) {
    private final transient SyncDateFormat format = new SyncDateFormat(
        new SimpleDateFormat("MMM dd yyyy, HH:mm"));

    @Transient
    @Override
    public SyncDateFormat getFormat() {
      return format;
    }
  },
  ISO_8601(4) {
    private final transient SyncDateFormat format = new SyncDateFormat(
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    @Transient
    @Override
    public SyncDateFormat getFormat() {
      return format;
    }
  }
  ;

  private final int labelOrdinal;

  AbsoluteDateTimeStyle(int labelOrdinal) {
    this.labelOrdinal = labelOrdinal;
  }

  @Transient
  public abstract SyncDateFormat getFormat();

  public String getLabel() {
    return ResBundle.message("absoluteDateTimeStyle.label", labelOrdinal);
  }
}

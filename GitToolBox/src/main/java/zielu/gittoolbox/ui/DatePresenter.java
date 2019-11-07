package zielu.gittoolbox.ui;

import java.time.ZonedDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.config.DateType;
import zielu.gittoolbox.util.AppUtil;

public interface DatePresenter {
  static DatePresenter getInstance() {
    return AppUtil.getServiceInstance(DatePresenter.class);
  }

  @Nullable
  String format(@NotNull DateType type, @Nullable ZonedDateTime date);
}

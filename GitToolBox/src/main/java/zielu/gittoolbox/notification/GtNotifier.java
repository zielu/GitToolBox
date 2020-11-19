package zielu.gittoolbox.notification;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.AppUtil;

public interface GtNotifier {
  @NotNull
  static GtNotifier getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, GtNotifier.class);
  }

  @NotNull
  Notification behindTrackerSuccess(@NotNull String message);

  @NotNull
  Notification behindTrackerSuccess(@NotNull String title, @NotNull String message,
                                    @Nullable NotificationListener listener);

  @NotNull
  Notification fetchError(@NotNull String message);

  @NotNull
  Notification fetchInfo(@NotNull String title, @NotNull String message);

  @NotNull
  Notification fetchWarning(@NotNull String title, @NotNull String message);

  @NotNull
  Notification autoFetchInfo(@NotNull String title, @NotNull String message);
}

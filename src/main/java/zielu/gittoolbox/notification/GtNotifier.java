package zielu.gittoolbox.notification;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.AppUtil;

public interface GtNotifier {
  NotificationGroup BEHIND_TRACKER_GROUP_ID = NotificationGroup.toolWindowGroup(
      "GitToolBox Behind Tracker", ChangesViewContentManager.TOOLWINDOW_ID);
  NotificationGroup FETCH_GROUP_ID = NotificationGroup.toolWindowGroup(
      "GitToolBox Fetch", ChangesViewContentManager.TOOLWINDOW_ID);

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

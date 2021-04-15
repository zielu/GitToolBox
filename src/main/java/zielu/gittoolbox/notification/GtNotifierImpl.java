package zielu.gittoolbox.notification;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsNotifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class GtNotifierImpl implements GtNotifier {
  private final Project project;
  private final NotificationGroup behindTrackerGroup;
  private final NotificationGroup fetchGroup;
  private final NotificationGroup cleanupGroup;

  GtNotifierImpl(@NotNull Project project) {
    this.project = project;
    var groupManager = NotificationGroupManager.getInstance();
    behindTrackerGroup = groupManager.getNotificationGroup("gittoolbox.behind.tracker");
    fetchGroup = groupManager.getNotificationGroup("gittoolbox.fetch");
    cleanupGroup = groupManager.getNotificationGroup("gittoolbox.branch.cleanup");
  }

  @NotNull
  @Override
  public Notification behindTrackerSuccess(@NotNull String message) {
    return behindTrackerSuccess("", message, null);
  }

  @NotNull
  @Override
  public Notification behindTrackerSuccess(@NotNull String title, @NotNull String message,
                                           @Nullable NotificationListener listener) {
    return notify(behindTrackerGroup, title, message, NotificationType.INFORMATION, listener);
  }

  @NotNull
  @Override
  public Notification fetchError(@NotNull String message) {
    return notify(fetchGroup, "", message, NotificationType.ERROR, null);
  }

  @NotNull
  @Override
  public Notification fetchInfo(@NotNull String title, @NotNull String message) {
    return fetchInfo(title, message, null);
  }

  @NotNull
  private Notification fetchInfo(@NotNull String title, @NotNull String message,
                                 @Nullable NotificationListener listener) {
    return notify(fetchGroup, title, message, NotificationType.INFORMATION, listener);
  }

  @NotNull
  @Override
  public Notification fetchWarning(@NotNull String title, @NotNull String message) {
    return fetchWarning(title, message, null);
  }

  @NotNull
  private Notification fetchWarning(@NotNull String title, @NotNull String message,
                                    @Nullable NotificationListener listener) {
    return notify(fetchGroup, title, message, NotificationType.WARNING, listener);
  }

  @NotNull
  @Override
  public Notification autoFetchInfo(@NotNull String title, @NotNull String message) {
    return notify(VcsNotifier.SILENT_NOTIFICATION, title, message, NotificationType.INFORMATION, null);
  }

  @Override
  public Notification branchCleanupSuccess(@NotNull String title, @NotNull String message) {
    return notify(cleanupGroup, title, message, NotificationType.INFORMATION, null);
  }

  @NotNull
  private Notification createNotification(@NotNull NotificationGroup notificationGroup,
                                          @NotNull String title,
                                          @NotNull String message,
                                          @NotNull NotificationType type,
                                          @Nullable NotificationListener listener) {
    // title can be empty; message can't be neither null, nor empty
    if (StringUtil.isEmptyOrSpaces(message)) {
      message = title;
      title = "";
    }
    // if both title and message were empty, then it is a problem in the calling code =>
    // Notifications engine assertion will notify.
    return notificationGroup.createNotification(title, message, type, listener);
  }

  @NotNull
  private Notification notify(@NotNull NotificationGroup notificationGroup,
                              @NotNull String title,
                              @NotNull String message,
                              @NotNull NotificationType type,
                              @Nullable NotificationListener listener) {
    Notification notification = createNotification(notificationGroup, title, message, type, listener);
    return notify(notification);
  }

  @NotNull
  private Notification notify(@NotNull Notification notification) {
    notification.notify(project);
    return notification;
  }
}

package zielu.gittoolbox.compat;

import com.google.common.base.Preconditions;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsNotifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Notifier {
  private final Project project;

  private Notifier(Project project) {
    this.project = project;
  }

  public static Notifier getInstance(@NotNull Project project) {
    return new Notifier(Preconditions.checkNotNull(project));
  }

  public NotificationHandle notifySuccess(@NotNull String message) {
    return NotificationHandleImpl.create(VcsNotifier.getInstance(project).notifySuccess(message));
  }

  public NotificationHandle notifySuccess(@NotNull String message, @Nullable NotificationListener listener) {
    return NotificationHandleImpl.create(
        VcsNotifier.getInstance(project).notifySuccess(message, "", listener)
    );
  }

  public NotificationHandle notifyError(@NotNull String title, @NotNull String message) {
    return NotificationHandleImpl.create(VcsNotifier.getInstance(project).notifyError(title, message));
  }

  public NotificationHandle notifyWeakError(@NotNull String message) {
    return NotificationHandleImpl.create(VcsNotifier.getInstance(project).notifyWeakError(message));
  }

  public NotificationHandle notifyMinorInfo(@NotNull String title, @NotNull String message) {
    return NotificationHandleImpl.create(VcsNotifier.getInstance(project).notifyMinorInfo(title, message));
  }

  public NotificationHandle notifyMinorWarning(@NotNull String title, @NotNull String message) {
    return NotificationHandleImpl.create(VcsNotifier.getInstance(project).notifyMinorWarning(title, message));
  }

  public NotificationHandle notifyLogWithPopup(@NotNull String title, @NotNull String message) {
    if (StringUtil.isEmptyOrSpaces(message)) {
      message = title;
      title = "";
    }
    NotificationGroup group = VcsNotifier.NOTIFICATION_GROUP_ID;
    Notification notification = group.createNotification(title, message, NotificationType.INFORMATION, null);
    notification.notify(project);
    return NotificationHandleImpl.create(notification);
  }

  public NotificationHandle notifyLogOnly(@NotNull String title, @NotNull String message) {
    return NotificationHandleImpl.create(VcsNotifier.getInstance(project).logInfo(title, message));
  }
}

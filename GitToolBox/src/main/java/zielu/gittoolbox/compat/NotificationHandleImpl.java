package zielu.gittoolbox.compat;

import com.intellij.notification.Notification;

public class NotificationHandleImpl implements NotificationHandle {
  private final Notification notification;

  private NotificationHandleImpl(Notification notification) {
    this.notification = notification;
  }

  public static NotificationHandle create(Notification notification) {
    return new NotificationHandleImpl(notification);
  }

  @Override
  public void expire() {
    notification.expire();
  }
}

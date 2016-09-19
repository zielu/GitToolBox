package zielu.gittoolbox.compat;

import com.intellij.notification.Notification;

public class NotificationHandleImpl implements NotificationHandle {
    private final Notification myNotification;

    private NotificationHandleImpl(Notification notification) {
        myNotification = notification;
    }

    public static NotificationHandle create(Notification notification) {
        return new NotificationHandleImpl(notification);
    }

    @Override
    public void expire() {
        myNotification.expire();
    }
}

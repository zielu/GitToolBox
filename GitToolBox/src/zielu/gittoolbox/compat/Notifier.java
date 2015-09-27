package zielu.gittoolbox.compat;

import com.google.common.base.Preconditions;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsNotifier;
import org.jetbrains.annotations.NotNull;

public class Notifier {
    private final Project myProject;

    private Notifier(Project project) {
        myProject = project;
    }

    public static Notifier getInstance(@NotNull Project project) {
        return new Notifier(Preconditions.checkNotNull(project));
    }

    public NotificationHandle notifySuccess(@NotNull String message) {
        return NotificationHandleImpl.create(VcsNotifier.getInstance(myProject).notifySuccess(message));
    }

    public NotificationHandle notifyError(@NotNull String title, @NotNull String message) {
        return NotificationHandleImpl.create(VcsNotifier.getInstance(myProject).notifyError(title, message));
    }

    public NotificationHandle notifyWeakError(@NotNull String message) {
        return NotificationHandleImpl.create(VcsNotifier.getInstance(myProject).notifyWeakError(message));
    }

    public NotificationHandle notifyMinorInfo(@NotNull String title, @NotNull String message) {
        return NotificationHandleImpl.create(VcsNotifier.getInstance(myProject).notifyMinorInfo(title, message));
    }

    public NotificationHandle notifyMinorWarning(@NotNull String title, @NotNull String message) {
        return NotificationHandleImpl.create(VcsNotifier.getInstance(myProject).notifyMinorWarning(title, message));
    }

    public NotificationHandle notifyLogWithPopup(@NotNull String title, @NotNull String message) {
        if(StringUtil.isEmptyOrSpaces(message)) {
            message = title;
            title = "";
        }
        NotificationGroup group = VcsNotifier.NOTIFICATION_GROUP_ID;
        Notification notification = group.createNotification(title, message, NotificationType.INFORMATION, null);
        notification.notify(myProject);
        return NotificationHandleImpl.create(notification);
    }

    public NotificationHandle notifyLogOnly(@NotNull String title, @NotNull String message) {
        return NotificationHandleImpl.create(VcsNotifier.getInstance(myProject).logInfo(title, message));
    }
}

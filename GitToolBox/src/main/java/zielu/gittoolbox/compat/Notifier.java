package zielu.gittoolbox.compat;

import com.google.common.base.Preconditions;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.notification.GtNotifier;

public class Notifier {
  private final Project project;

  private Notifier(Project project) {
    this.project = project;
  }

  public static Notifier getInstance(@NotNull Project project) {
    return new Notifier(Preconditions.checkNotNull(project));
  }

  public NotificationHandle behindTrackerSuccess(@NotNull String message) {
    return NotificationHandleImpl.create(GtNotifier.getInstance(project).behindTrackerSuccess(message));
  }

  public NotificationHandle behindTrackerSuccess(@NotNull String message, @Nullable NotificationListener listener) {
    return NotificationHandleImpl.create(
        GtNotifier.getInstance(project).behindTrackerSuccess("", message, listener)
    );
  }

  public NotificationHandle fetchError(@NotNull String message) {
    return NotificationHandleImpl.create(GtNotifier.getInstance(project).fetchError(message));
  }

  public NotificationHandle fetchInfo(@NotNull String title, @NotNull String message) {
    return NotificationHandleImpl.create(GtNotifier.getInstance(project).fetchInfo(title, message));
  }

  public NotificationHandle fetchWarning(@NotNull String title, @NotNull String message) {
    return NotificationHandleImpl.create(GtNotifier.getInstance(project).fetchWarning(title, message));
  }

  public NotificationHandle autoFetchInfo(@NotNull String title, @NotNull String message) {
    return NotificationHandleImpl.create(GtNotifier.getInstance(project).autoFetchInfo(title, message));
  }
}

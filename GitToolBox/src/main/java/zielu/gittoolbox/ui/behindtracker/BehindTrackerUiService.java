package zielu.gittoolbox.ui.behindtracker;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import javax.swing.event.HyperlinkEvent;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.ui.StatusMessages;
import zielu.gittoolbox.ui.UpdateProject;

class BehindTrackerUiService implements BehindTrackerUi {
  private final Project project;
  private final NotificationListener updateProjectListener;

  BehindTrackerUiService(@NotNull Project project) {
    this.project = project;
    updateProjectListener = new NotificationListener.Adapter() {
      @Override
      protected void hyperlinkActivated(@NotNull Notification notification,
                                        @NotNull HyperlinkEvent hyperlinkEvent) {
        UpdateProject.create(project).execute();
      }
    };
    ;
  }

  @Override
  public void displaySuccessNotification(String message) {
    Notifier.getInstance(project).behindTrackerSuccess(message, updateProjectListener);
  }

  @Override
  public boolean isNotificationEnabled() {
    return GitToolBoxConfig.getInstance().behindTracker;
  }

  @Override
  public StatusMessages getStatusMessages() {
    return ServiceManager.getService(StatusMessages.class);
  }
}

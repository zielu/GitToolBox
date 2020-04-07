package zielu.gittoolbox.ui.behindtracker;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import javax.swing.event.HyperlinkEvent;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.config.AppConfig;
import zielu.gittoolbox.repo.GtRepository;
import zielu.gittoolbox.repo.RepoKt;
import zielu.gittoolbox.ui.StatusMessagesService;
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
        UpdateProject.create(project).execute(hyperlinkEvent.getInputEvent());
      }
    };
  }

  @Override
  public void displaySuccessNotification(@NotNull String message) {
    Notifier.getInstance(project).behindTrackerSuccess(message, updateProjectListener);
  }

  @Override
  public boolean isNotificationEnabled() {
    return AppConfig.get().getBehindTracker();
  }

  @Override
  public StatusMessagesService getStatusMessages() {
    return StatusMessagesService.getInstance();
  }

  @Override
  public GtRepository getGtRepository(GitRepository repository) {
    return RepoKt.createGtRepository(repository);
  }
}

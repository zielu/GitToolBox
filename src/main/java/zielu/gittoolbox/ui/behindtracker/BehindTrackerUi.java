package zielu.gittoolbox.ui.behindtracker;

import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.repo.GtRepository;
import zielu.gittoolbox.ui.StatusMessagesService;
import zielu.gittoolbox.util.AppUtil;

public interface BehindTrackerUi {

  static BehindTrackerUi getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, BehindTrackerUi.class);
  }

  void displaySuccessNotification(@NotNull String message);

  boolean isNotificationEnabled();

  StatusMessagesService getStatusMessages();

  GtRepository getGtRepository(GitRepository repository);
}

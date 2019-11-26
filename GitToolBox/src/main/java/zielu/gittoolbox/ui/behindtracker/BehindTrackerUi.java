package zielu.gittoolbox.ui.behindtracker;

import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.repo.GtRepository;
import zielu.gittoolbox.ui.StatusMessagesService;

public interface BehindTrackerUi {
  void displaySuccessNotification(@NotNull String message);

  boolean isNotificationEnabled();

  StatusMessagesService getStatusMessages();

  GtRepository getGtRepository(GitRepository repository);
}

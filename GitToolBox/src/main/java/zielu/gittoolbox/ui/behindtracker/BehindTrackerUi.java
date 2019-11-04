package zielu.gittoolbox.ui.behindtracker;

import zielu.gittoolbox.ui.StatusMessagesService;

public interface BehindTrackerUi {
  void displaySuccessNotification(String message);

  boolean isNotificationEnabled();

  StatusMessagesService getStatusMessages();
}

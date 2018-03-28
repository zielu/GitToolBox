package zielu.gittoolbox.ui.behindtracker;

import zielu.gittoolbox.ui.StatusMessages;

public interface BehindTrackerUi {
  void displaySuccessNotification(String message);

  boolean isNotificationEnabled();

  StatusMessages getStatusMessages();
}

package zielu.gittoolbox.ui;

import zielu.gittoolbox.util.AppUtil;

public interface StatusMessagesUi {

  static StatusMessagesUi getInstance() {
    return AppUtil.getServiceInstance(StatusMessagesUi.class);
  }

  StatusPresenter presenter();
}

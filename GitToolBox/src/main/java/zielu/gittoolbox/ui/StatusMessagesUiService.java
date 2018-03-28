package zielu.gittoolbox.ui;

import zielu.gittoolbox.config.GitToolBoxConfig;

public class StatusMessagesUiService implements StatusMessagesUi {
  @Override
  public StatusPresenter presenter() {
    return GitToolBoxConfig.getInstance().getPresenter();
  }
}

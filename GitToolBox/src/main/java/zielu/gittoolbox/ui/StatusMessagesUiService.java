package zielu.gittoolbox.ui;

import zielu.gittoolbox.config.GitToolBoxConfig2;

public class StatusMessagesUiService implements StatusMessagesUi {
  @Override
  public StatusPresenter presenter() {
    return GitToolBoxConfig2.getInstance().getPresenter();
  }
}

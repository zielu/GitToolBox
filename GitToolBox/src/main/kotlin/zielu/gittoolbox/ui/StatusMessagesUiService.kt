package zielu.gittoolbox.ui

import zielu.gittoolbox.config.GitToolBoxConfig2

internal class StatusMessagesUiService : StatusMessagesUi {
  override fun presenter(): StatusPresenter = GitToolBoxConfig2.getInstance().getPresenter()
}

package zielu.gittoolbox.ui

import zielu.gittoolbox.config.AppConfig

internal class StatusMessagesUiService : StatusMessagesUi {
  override fun presenter(): StatusPresenter = AppConfig.get().getPresenter()
}

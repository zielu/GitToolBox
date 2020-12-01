package zielu.gittoolbox.ui

import zielu.gittoolbox.util.AppUtil

internal interface StatusMessagesUi {
  fun presenter(): StatusPresenter

  companion object {
    fun getInstance() = AppUtil.getServiceInstance(StatusMessagesUi::class.java)
  }
}

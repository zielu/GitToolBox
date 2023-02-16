package zielu.gittoolbox.ui.config.v2.app

import zielu.gittoolbox.ui.StatusPresenter
import zielu.gittoolbox.ui.StatusPresenters

internal class AppPages {
  var statusPresenter: StatusPresenter = StatusPresenters.defaultPresenter()
    set(presenter) {
      field = presenter
      notifyChanged()
    }

  private val listeners = arrayListOf<AppPagesNotifier>()

  fun addListener(listener: AppPagesNotifier) {
    listeners.add(listener)
  }

  private fun notifyChanged() {
    listeners.forEach { it.appPagesChanged(this) }
  }
}

internal interface AppPagesNotifier {
  fun appPagesChanged(appPages: AppPages)
}

package zielu.gittoolbox.ui

import zielu.gittoolbox.config.DateType
import zielu.gittoolbox.util.AppUtil
import java.time.ZonedDateTime

internal interface DatePresenter {

  fun format(type: DateType, date: ZonedDateTime): String

  companion object {
    @JvmStatic
    fun getInstance() = AppUtil.getServiceInstance(DatePresenter::class.java)
  }
}

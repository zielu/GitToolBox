package zielu.gittoolbox.ui

import com.intellij.util.text.SyncDateFormat
import zielu.gittoolbox.config.AppConfig
import java.time.Clock

internal class DatePresenterFacade {
  fun getAbsoluteDateTimeFormat(): SyncDateFormat = AppConfig.getConfig()
    .absoluteDateTimeStyle.format

  fun getClock(): Clock = Clock.systemDefaultZone()
}

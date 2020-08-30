package zielu.gittoolbox.ui

import com.intellij.util.text.SyncDateFormat
import zielu.gittoolbox.config.AppConfig
import java.time.Clock

internal class DatePresenterLocalGatewayImpl : DatePresenterLocalGateway {
  override fun getAbsoluteDateTimeFormat(): SyncDateFormat = AppConfig.getConfig()
    .absoluteDateTimeStyle.format

  override fun getClock(): Clock = Clock.systemDefaultZone()
}

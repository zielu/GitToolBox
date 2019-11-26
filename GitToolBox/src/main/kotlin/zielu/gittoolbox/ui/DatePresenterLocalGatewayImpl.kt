package zielu.gittoolbox.ui

import com.intellij.util.text.SyncDateFormat
import zielu.gittoolbox.config.GitToolBoxConfig2
import java.time.Clock

internal class DatePresenterLocalGatewayImpl : DatePresenterLocalGateway {
  override fun getAbsoluteDateTimeFormat(): SyncDateFormat = GitToolBoxConfig2.getInstance()
    .absoluteDateTimeStyle.format

  override fun getClock(): Clock = Clock.systemDefaultZone()
}

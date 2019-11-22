package zielu.gittoolbox.ui

import com.intellij.util.text.SyncDateFormat
import java.time.Clock

internal interface DatePresenterLocalGateway {
  fun getAbsoluteDateTimeFormat(): SyncDateFormat
  fun getClock(): Clock
}

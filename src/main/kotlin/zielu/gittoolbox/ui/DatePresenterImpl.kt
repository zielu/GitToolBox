package zielu.gittoolbox.ui

import com.intellij.serviceContainer.NonInjectable
import com.intellij.util.text.SyncDateFormat
import zielu.gittoolbox.config.DateType
import zielu.intellij.util.ZDateFormatUtil
import java.time.ZonedDateTime
import java.util.Date

internal class DatePresenterImpl
@NonInjectable
constructor(private val facade: DatePresenterFacade) : DatePresenter {

  constructor() : this(DatePresenterFacade())

  override fun format(type: DateType, date: ZonedDateTime) = formatImpl(type, date)

  private fun formatImpl(dateType: DateType, dateTime: ZonedDateTime): String {
    return when (dateType) {
      DateType.AUTO -> formatPrettyDate(dateTime)
      DateType.ABSOLUTE -> formatAbsoluteDate(dateTime)
      DateType.RELATIVE -> formatRelative(dateTime)
      DateType.HIDDEN -> ""
    }
  }

  private fun formatPrettyDate(date: ZonedDateTime): String {
    return ZDateFormatUtil.formatPrettyDateTime(
      date,
      ZonedDateTime.now(facade.getClock()),
      getAbsoluteDateTimeFormat()
    )
  }

  private fun getAbsoluteDateTimeFormat(): SyncDateFormat = facade.getAbsoluteDateTimeFormat()

  private fun formatAbsoluteDate(date: ZonedDateTime): String {
    return getAbsoluteDateTimeFormat().format(Date.from(date.toInstant()))
  }

  private fun formatRelative(date: ZonedDateTime): String {
    val now = ZonedDateTime.ofInstant(facade.getClock().instant(), facade.getClock().zone)
    return ZDateFormatUtil.formatBetweenDates(date, now)
  }
}

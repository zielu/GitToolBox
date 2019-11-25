package zielu.gittoolbox.ui

import com.intellij.util.text.SyncDateFormat
import zielu.gittoolbox.config.DateType
import zielu.gittoolbox.util.DateFormattingUtil
import zielu.intellij.util.ZDateFormatUtil
import java.time.ZonedDateTime
import java.util.Date

internal class DatePresenterImpl
  constructor(private val gateway: DatePresenterLocalGateway) : DatePresenter {

  constructor() : this(DatePresenterLocalGatewayImpl())

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
    return ZDateFormatUtil.formatPrettyDateTime(date, ZonedDateTime.now(gateway.getClock()),
      getAbsoluteDateTimeFormat())
  }

  private fun getAbsoluteDateTimeFormat(): SyncDateFormat = gateway.getAbsoluteDateTimeFormat()

  private fun formatAbsoluteDate(date: ZonedDateTime): String {
    return getAbsoluteDateTimeFormat().format(Date.from(date.toInstant()))
  }

  private fun formatRelative(date: ZonedDateTime): String {
    val now = Date.from(gateway.getClock().instant())
    return DateFormattingUtil.formatRelativeBetweenDateTimes(Date.from(date.toInstant()), now)
  }
}

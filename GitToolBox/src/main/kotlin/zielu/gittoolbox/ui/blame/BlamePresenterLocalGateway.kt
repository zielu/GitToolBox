package zielu.gittoolbox.ui.blame

import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.config.AuthorNameType
import zielu.gittoolbox.config.DateType
import zielu.gittoolbox.ui.DatePresenter
import java.time.ZonedDateTime

internal class BlamePresenterLocalGateway {
  private val datePresenter by lazy {
    DatePresenter.getInstance()
  }

  fun getShowInlineSubject() = AppConfig.getConfig().blameInlineShowSubject

  fun getInlineAuthorNameType(): AuthorNameType = AppConfig.getConfig().blameInlineAuthorNameType

  fun getStatusAuthorNameTYpe(): AuthorNameType = AppConfig.getConfig().blameStatusAuthorNameType

  fun formatInlineDateTime(dateTime: ZonedDateTime): String {
    return datePresenter.format(AppConfig.getConfig().blameInlineDateType, dateTime)
  }

  fun formatDateTime(dateType: DateType, dateTime: ZonedDateTime) = datePresenter.format(dateType, dateTime)
}

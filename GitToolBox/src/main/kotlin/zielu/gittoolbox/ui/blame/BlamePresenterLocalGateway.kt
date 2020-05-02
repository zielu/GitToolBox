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

  fun getShowInlineSubject() = AppConfig.get().blameInlineShowSubject

  fun getInlineAuthorNameType(): AuthorNameType = AppConfig.get().blameInlineAuthorNameType

  fun getStatusAuthorNameTYpe(): AuthorNameType = AppConfig.get().blameStatusAuthorNameType

  fun formatInlineDateTime(dateTime: ZonedDateTime): String {
    return datePresenter.format(AppConfig.get().blameInlineDateType, dateTime)
  }

  fun formatDateTime(dateType: DateType, dateTime: ZonedDateTime) = datePresenter.format(dateType, dateTime)
}

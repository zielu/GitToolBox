package zielu.gittoolbox.ui.blame

import zielu.gittoolbox.config.AuthorNameType
import zielu.gittoolbox.config.DateType
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.ui.DatePresenter
import java.time.ZonedDateTime

internal class BlamePresenterLocalGateway {
  private val datePresenter by lazy {
    DatePresenter.getInstance()
  }

  fun getShowInlineSubject() = GitToolBoxConfig2.getInstance().blameInlineShowSubject

  fun getInlineAuthorNameType(): AuthorNameType = GitToolBoxConfig2.getInstance().blameInlineAuthorNameType

  fun getStatusAuthorNameTYpe(): AuthorNameType = GitToolBoxConfig2.getInstance().blameStatusAuthorNameType

  fun formatInlineDateTime(dateTime: ZonedDateTime): String {
    return datePresenter.format(GitToolBoxConfig2.getInstance().blameInlineDateType, dateTime)
  }

  fun formatDateTime(dateType: DateType, dateTime: ZonedDateTime) = datePresenter.format(dateType, dateTime)
}

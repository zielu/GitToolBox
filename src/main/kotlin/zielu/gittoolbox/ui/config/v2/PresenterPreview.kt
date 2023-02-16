package zielu.gittoolbox.ui.config.v2

import zielu.gittoolbox.status.BehindStatus
import zielu.gittoolbox.ui.StatusPresenter
import java.util.stream.Collectors
import java.util.stream.Stream

internal object PresenterPreview {
  private const val delimiter = " | "

  @JvmStatic
  fun getStatusBarPreview(presenter: StatusPresenter): String {
    return Stream.of(
      presenter.aheadBehindStatus(3, 2),
      presenter.aheadBehindStatus(3, 0),
      presenter.aheadBehindStatus(0, 2)
    ).collect(Collectors.joining(delimiter))
  }

  @JvmStatic
  fun getProjectViewPreview(presenter: StatusPresenter): String {
    return Stream.of(
      presenter.nonZeroAheadBehindStatus(3, 2),
      presenter.nonZeroAheadBehindStatus(3, 0),
      presenter.nonZeroAheadBehindStatus(0, 2),
      presenter.branchAndParent("feature", "master")
    ).collect(Collectors.joining(delimiter))
  }

  @JvmStatic
  fun getBehindTrackerPreview(presenter: StatusPresenter): String {
    return Stream.of(
      presenter.behindStatus(BehindStatus(3, 1)),
      presenter.behindStatus(BehindStatus(3, -1)),
      presenter.behindStatus(BehindStatus(3))
    ).collect(Collectors.joining(delimiter))
  }
}

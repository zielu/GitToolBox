package zielu.gittoolbox.ui.config.v2

import jodd.util.StringBand
import zielu.gittoolbox.config.DecorationPartType
import zielu.gittoolbox.ui.ExtendedRepoInfo
import zielu.gittoolbox.ui.StatusPresenter
import zielu.gittoolbox.util.Count

internal object DecorationPartPreview {
  private val previews = mapOf(
    Pair(DecorationPartType.BRANCH, "master"),
    Pair(DecorationPartType.LOCATION, "/path/to/location"),
    Pair(DecorationPartType.TAGS_ON_HEAD, "1.0.0"),
    Pair(DecorationPartType.CHANGED_COUNT, "1 changed")
  )

  @JvmStatic
  fun appendPreview(presenter: StatusPresenter, type: DecorationPartType, preview: StringBand): StringBand {
    return preview.append(getPreview(presenter, type))
  }

  private fun getPreview(presenter: StatusPresenter, type: DecorationPartType): String {
    return when (type) {
      DecorationPartType.STATUS -> {
        presenter.aheadBehindStatus(3, 2)
      }
      DecorationPartType.CHANGED_COUNT -> {
        presenter.extendedRepoInfo(ExtendedRepoInfo(Count(1)))
      }
      else -> {
        previews.getOrDefault(type, "N/A")
      }
    }
  }
}

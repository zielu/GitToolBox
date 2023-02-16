package zielu.gittoolbox.ui

import zielu.gittoolbox.util.Count

internal data class ExtendedRepoInfo(
  val changedCount: Count = Count.EMPTY
) {
  fun hasChanged(): Boolean {
    return !changedCount.isEmpty()
  }
}

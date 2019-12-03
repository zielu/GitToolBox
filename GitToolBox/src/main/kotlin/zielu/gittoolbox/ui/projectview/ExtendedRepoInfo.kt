package zielu.gittoolbox.ui.projectview

internal data class ExtendedRepoInfo(
  val changedCount: Int = -1
) {
  fun hasChanged(): Boolean {
    return changedCount >= 0
  }
}

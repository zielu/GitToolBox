package zielu.gittoolbox.changes

internal interface ChangesTrackerListener {
  fun changesCountChanged(changesCount: Int)
}

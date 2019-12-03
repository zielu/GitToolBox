package zielu.gittoolbox.changes

import com.intellij.util.messages.Topic

internal interface ChangesTrackerListener {
  fun changesCountChanged(changesCount: Int)
}

internal val CHANGES_TRACKER_TOPIC: Topic<ChangesTrackerListener> = Topic.create(
  "Git ToolBox Changes Notification",
  ChangesTrackerListener::class.java
)

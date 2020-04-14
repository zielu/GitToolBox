package zielu.gittoolbox.changes

import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.LocalChangeList
import zielu.intellij.java.mapNotNull
import kotlin.streams.toList

internal data class ChangeListData(
  val id: String,
  val changes: Collection<ChangeData>
) {
  val hasChanges: Boolean
    get() = !changes.isEmpty()

  constructor(localChangeList: LocalChangeList) : this(localChangeList.id, createChangeData(localChangeList.changes))

  private companion object {
    private fun createChangeData(changes: Collection<Change>): Collection<ChangeData> {
      return changes
        .stream()
        .mapNotNull { getFilePath(it) }
        .map { ChangeData(it) }
        .toList()
    }

    private fun getFilePath(change: Change): FilePath? {
      return change.afterRevision?.file ?: change.beforeRevision?.file
    }
  }
}

internal data class ChangeData(
  val filePath: FilePath
)

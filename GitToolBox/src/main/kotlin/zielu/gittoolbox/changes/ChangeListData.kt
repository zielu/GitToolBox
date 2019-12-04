package zielu.gittoolbox.changes

import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.LocalChangeList

internal data class ChangeListData(
  val id: String,
  val changes: Collection<Change>
) {
  constructor(localChangeList: LocalChangeList) : this(localChangeList.id, ArrayList(localChangeList.changes))

  fun hasChanges(): Boolean = !changes.isEmpty()
}

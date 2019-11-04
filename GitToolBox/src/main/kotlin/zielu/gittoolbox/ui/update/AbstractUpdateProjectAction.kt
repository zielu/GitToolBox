package zielu.gittoolbox.ui.update

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import zielu.gittoolbox.extension.UpdateProjectAction

internal abstract class AbstractUpdateProjectAction(
  private val id: String,
  private val actionId: String
) : UpdateProjectAction {

  override fun getId(): String = id
  override fun getAction(): AnAction {
    return ActionManager.getInstance().getAction(actionId)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as AbstractUpdateProjectAction

    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}

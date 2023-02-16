package zielu.gittoolbox.extension.update

import com.intellij.openapi.actionSystem.AnAction
import zielu.gittoolbox.ResBundle

internal interface UpdateProjectAction {
  fun getId(): String

  fun isDefault(): Boolean

  fun getName(): String {
    return ResBundle.message(getId())
  }

  fun getAction(): AnAction
}

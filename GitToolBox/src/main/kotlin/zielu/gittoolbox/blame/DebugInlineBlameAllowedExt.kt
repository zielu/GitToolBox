package zielu.gittoolbox.blame

import com.intellij.openapi.project.Project
import zielu.gittoolbox.extension.blame.InlineBlameAllowed

internal class DebugInlineBlameAllowedExt : InlineBlameAllowed {
  override fun isAllowed(project: Project): Boolean {
    return DebugInlineBlameAllowed.getInstance(project).isAllowed()
  }
}

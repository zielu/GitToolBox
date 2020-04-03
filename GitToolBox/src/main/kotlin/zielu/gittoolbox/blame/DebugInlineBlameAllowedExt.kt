package zielu.gittoolbox.blame

import com.intellij.openapi.project.Project
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.extension.blame.InlineBlameAllowed

internal class DebugInlineBlameAllowedExt : InlineBlameAllowed {
  override fun isAllowed(project: Project): Boolean {
    return if (GitToolBoxConfig2.getInstance().alwaysShowInlineBlameWhileDebugging) {
      return true
    } else {
      DebugInlineBlameAllowed.getInstance(project).isAllowed()
    }
  }
}

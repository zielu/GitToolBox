package zielu.gittoolbox.blame

import com.intellij.openapi.project.Project
import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.extension.blame.InlineBlameAllowed

internal class DebugInlineBlameAllowedExt : InlineBlameAllowed {
  override fun isAllowed(project: Project): Boolean {
    return if (AppConfig.get().alwaysShowInlineBlameWhileDebugging) {
      return true
    } else {
      DebugInlineBlameAllowed.getInstance(project).isAllowed()
    }
  }
}

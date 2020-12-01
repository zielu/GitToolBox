package zielu.gittoolbox.extension.blame

import com.intellij.openapi.project.Project

internal interface InlineBlameAllowed {
  fun isAllowed(project: Project): Boolean
}

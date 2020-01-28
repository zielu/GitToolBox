package zielu.gittoolbox.fetch

import com.intellij.openapi.project.Project
import zielu.gittoolbox.extension.autofetch.AutoFetchAllowed

internal class AutoFetchAllowedDumbModeExt : AutoFetchAllowed {

  override fun isAllowed(project: Project): Boolean {
    return AutoFetchAllowedDumbMode.getInstance(project).isAllowed()
  }
}

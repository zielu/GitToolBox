package zielu.gittoolbox.fetch

import com.intellij.openapi.project.Project
import zielu.gittoolbox.extension.autofetch.AutoFetchAllowed

internal class AutoFetchAllowedBuildExt : AutoFetchAllowed {

  override fun isAllowed(project: Project): Boolean {
    return AutoFetchAllowedBuild.getInstance(project)
      .map { it.isFetchAllowed() }
      .orElse(true)
  }
}

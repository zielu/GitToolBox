package zielu.gittoolbox.extension.autofetch

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

internal class AutoFetchAllowedExtension(private val project: Project) {
  fun isFetchAllowed(): Boolean {
    return extensions().all { ext -> ext.isAllowed(project) }
  }

  private fun extensions(): List<AutoFetchAllowed> {
    return EXTENSION_POINT_NAME.extensionList
      .map { ext -> ext.instantiate() }
  }
}

private val EXTENSION_POINT_NAME: ExtensionPointName<AutoFetchAllowedEP> = ExtensionPointName.create(
  "zielu.gittoolbox.autoFetchAllowed"
)

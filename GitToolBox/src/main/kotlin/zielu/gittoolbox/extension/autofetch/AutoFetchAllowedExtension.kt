package zielu.gittoolbox.extension.autofetch

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

internal class AutoFetchAllowedExtension(private val project: Project) {
  private val extensions by lazy {
    EXTENSION_POINT_NAME.extensionList
      .map { ext -> ext.instantiate(project) }
      .onEach { ext -> ext.initialize() }
  }

  fun isFetchAllowed() = extensions.all { ext -> ext.isAllowed() }
}

private val EXTENSION_POINT_NAME: ExtensionPointName<AutoFetchAllowedEP> = ExtensionPointName.create(
  "zielu.gittoolbox.autoFetchAllowed"
)

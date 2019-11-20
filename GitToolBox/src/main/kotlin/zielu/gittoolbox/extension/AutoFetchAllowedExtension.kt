package zielu.gittoolbox.extension

import com.intellij.openapi.project.Project

internal class AutoFetchAllowedExtension(private val project: Project) {
  private val extensions by lazy {
    AutoFetchAllowedEP.POINT_NAME.extensionList
      .map { ext -> ext.instantiate(project) }
      .onEach { ext -> ext.initialize() }
  }

  fun isFetchAllowed() = extensions.all { ext -> ext.isAllowed }
}

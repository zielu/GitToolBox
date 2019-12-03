package zielu.gittoolbox.extension.update

import com.intellij.openapi.extensions.ExtensionPointName

internal class UpdateProjectActionExtension {
  private val extensions by lazy {
    EXTENSION_POINT_NAME.extensionList
      .map { ext -> ext.instantiate() }
  }

  fun getAll(): List<UpdateProjectAction> = extensions

  fun getById(id: String): UpdateProjectAction? {
    return extensions.first { ext -> ext.getId() == id }
  }

  fun getDefault(): UpdateProjectAction {
    return extensions.first { ext -> ext.isDefault() }
  }
}

private val EXTENSION_POINT_NAME: ExtensionPointName<UpdateProjectActionEP> = ExtensionPointName.create(
  "zielu.gittoolbox.updateProjectAction")

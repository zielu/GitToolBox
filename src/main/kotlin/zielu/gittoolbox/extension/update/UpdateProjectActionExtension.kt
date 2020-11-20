package zielu.gittoolbox.extension.update

import com.intellij.openapi.extensions.ExtensionPointName

internal class UpdateProjectActionExtension {
  fun getAll(): List<UpdateProjectAction> {
    return EXTENSION_POINT_NAME.extensionList
      .map { ext -> ext.instantiate() }
  }

  fun getById(id: String): UpdateProjectAction? {
    return getAll().first { ext -> ext.getId() == id }
  }

  fun getDefault(): UpdateProjectAction {
    return getAll().first { ext -> ext.isDefault() }
  }
}

private val EXTENSION_POINT_NAME: ExtensionPointName<UpdateProjectActionEP> = ExtensionPointName.create(
  "zielu.gittoolbox.updateProjectAction"
)

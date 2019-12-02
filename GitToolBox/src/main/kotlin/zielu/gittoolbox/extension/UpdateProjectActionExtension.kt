package zielu.gittoolbox.extension

internal class UpdateProjectActionExtension {
  private val extensions by lazy {
    UpdateProjectActionEP.POINT_NAME.extensionList
      .map { ext -> ext.instantiate() }
  }

  fun getAll(): List<UpdateProjectAction> = extensions

  fun getById(id: String): UpdateProjectAction? {
    return extensions.first { ext -> ext.id == id }
  }

  fun getDefault(): UpdateProjectAction {
    return extensions.first { ext -> ext.isDefault }
  }
}

package zielu.gittoolbox.ui.update

import zielu.gittoolbox.extension.UpdateProjectAction
import zielu.gittoolbox.extension.UpdateProjectActionExtension
import zielu.gittoolbox.util.AppUtil

internal class UpdateProjectActionService {
  private val extension = UpdateProjectActionExtension()

  fun getAll(): List<UpdateProjectAction> = extension.getAll()

  fun getById(id: String): UpdateProjectAction {
    return extension.getById(id) ?: extension.getDefault()
  }

  companion object {
    @JvmStatic
    fun getInstance() = AppUtil.getServiceInstance(UpdateProjectActionService::class.java)
  }
}

package zielu.gittoolbox

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import zielu.gittoolbox.util.AppUtil

internal class GitToolBoxPrj : Disposable {
  override fun dispose() {
    // do nothing
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): GitToolBoxPrj {
      return AppUtil.getServiceInstance(project, GitToolBoxPrj::class.java)
    }
  }
}

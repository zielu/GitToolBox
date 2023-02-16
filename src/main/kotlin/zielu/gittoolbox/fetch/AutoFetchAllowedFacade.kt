package zielu.gittoolbox.fetch

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import zielu.gittoolbox.extension.autofetch.AUTO_FETCH_ALLOWED_TOPIC
import zielu.gittoolbox.util.PrjBaseFacade

internal class AutoFetchAllowedFacade(project: Project) : PrjBaseFacade(project) {

  fun fireStateChanged(disposable: Disposable) {
    publishAsync(disposable) { it.syncPublisher(AUTO_FETCH_ALLOWED_TOPIC).stateChanged() }
  }
}

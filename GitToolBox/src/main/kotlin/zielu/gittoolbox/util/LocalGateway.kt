package zielu.gittoolbox.util

import com.google.common.cache.Cache
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import zielu.gittoolbox.metrics.CacheMetrics
import zielu.gittoolbox.metrics.Metrics
import zielu.gittoolbox.metrics.ProjectMetrics

internal abstract class LocalGateway(private val project: Project) {
  fun getMetrics(): Metrics {
    return ProjectMetrics.getInstance(project)
  }

  fun exposeCacheMetrics(cache: Cache<*, *>, cacheName: String) {
    CacheMetrics.expose(cache, getMetrics(), cacheName)
  }

  fun disposeWithProject(disposable: Disposable) {
    Disposer.register(project, disposable)
  }

  protected fun runInBackground(task: () -> Unit) {
    ApplicationManager.getApplication().executeOnPooledThread(DisposeSafeRunnable(project, task))
  }
}

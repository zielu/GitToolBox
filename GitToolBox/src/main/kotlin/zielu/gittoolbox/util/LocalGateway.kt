package zielu.gittoolbox.util

import com.google.common.cache.Cache
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBus
import zielu.gittoolbox.lifecycle.PluginUnload
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

  protected fun publishSync(publisher: (messageBus: MessageBus) -> Unit) {
    if (PluginUnload.isInactive()) {
      publisher.invoke(project.messageBus)
    }
  }

  protected fun publishAsync(publisher: (messageBus: MessageBus) -> Unit) {
    runInBackground { publisher.invoke(project.messageBus) }
  }

  protected fun runInBackground(task: () -> Unit) {
    if (PluginUnload.isInactive()) {
      ApplicationManager.getApplication().executeOnPooledThread(DisposeSafeRunnable(project, task))
    }
  }
}

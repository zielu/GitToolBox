package zielu.gittoolbox.util

import com.google.common.cache.Cache
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBus
import zielu.gittoolbox.GitToolBoxApp
import zielu.gittoolbox.metrics.CacheMetrics
import zielu.gittoolbox.metrics.ProjectMetrics
import zielu.intellij.concurrent.DisposeSafeRunnable
import zielu.intellij.concurrent.ZDisposableRunnableWrapper
import zielu.intellij.metrics.Metrics

internal abstract class PrjBaseFacade(
  private val project: Project
) : BaseFacade() {
  fun getMetrics(): Metrics {
    return ProjectMetrics.getInstance(project)
  }

  fun exposeCacheMetrics(cache: Cache<*, *>, cacheName: String) {
    CacheMetrics.expose(cache, getMetrics(), cacheName)
  }

  protected fun publishSync(publisher: (messageBus: MessageBus) -> Unit) {
    GitToolBoxApp.getInstance().ifPresent { it.publishSync(project, publisher) }
  }

  protected fun publishAsync(disposable: Disposable, publisher: (messageBus: MessageBus) -> Unit) {
    val task = ZDisposableRunnableWrapper { publisher.invoke(project.messageBus) }
    registerDisposable(disposable, task)
    GitToolBoxApp.getInstance().ifPresent { it.runInBackground(DisposeSafeRunnable(task)) }
  }

  protected fun runInBackground(disposable: Disposable, task: () -> Unit) {
    val disposableTask = ZDisposableRunnableWrapper { task.invoke() }
    registerDisposable(disposable, disposableTask)
    GitToolBoxApp.getInstance().ifPresent { it.runInBackground(disposableTask) }
  }
}

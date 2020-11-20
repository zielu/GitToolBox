package zielu.gittoolbox.util

import com.google.common.cache.Cache
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.util.messages.MessageBus
import zielu.gittoolbox.GitToolBoxApp
import zielu.gittoolbox.metrics.CacheMetrics
import zielu.intellij.metrics.Metrics
import zielu.gittoolbox.metrics.ProjectMetrics
import zielu.intellij.concurrent.DisposeSafeRunnable
import zielu.intellij.concurrent.ZDisposableRunnableWrapper

internal abstract class LocalGateway(private val project: Project) {
  fun getMetrics(): Metrics {
    return ProjectMetrics.getInstance(project)
  }

  fun exposeCacheMetrics(cache: Cache<*, *>, cacheName: String) {
    CacheMetrics.expose(cache, getMetrics(), cacheName)
  }

  fun registerDisposable(parent: Disposable, child: Disposable) {
    Disposer.register(parent, child)
  }

  fun dispose(subject: Disposable) {
    Disposer.dispose(subject)
  }

  protected fun publishSync(publisher: (messageBus: MessageBus) -> Unit) {
    GitToolBoxApp.getInstance().ifPresent { it.publishSync(project, publisher) }
  }

  protected fun publishAsync(disposable: Disposable, publisher: (messageBus: MessageBus) -> Unit) {
    val task = ZDisposableRunnableWrapper(Runnable { publisher.invoke(project.messageBus) })
    registerDisposable(disposable, task)
    GitToolBoxApp.getInstance().ifPresent { it.runInBackground(DisposeSafeRunnable(task)) }
  }

  protected fun runInBackground(disposable: Disposable, task: () -> Unit) {
    val disposableTask = ZDisposableRunnableWrapper(Runnable { task.invoke() })
    registerDisposable(disposable, disposableTask)
    GitToolBoxApp.getInstance().ifPresent { it.runInBackground(disposableTask) }
  }
}

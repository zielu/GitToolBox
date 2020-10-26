package zielu.gittoolbox

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBus
import zielu.gittoolbox.metrics.AppMetrics
import zielu.gittoolbox.metrics.ThreadGroupMetrics
import zielu.gittoolbox.util.AppUtil
import zielu.gittoolbox.util.ConcurrentUtil
import zielu.intellij.concurrent.ThreadGroupThreadFactory
import java.util.Collections
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier

internal class GitToolBoxApp : Disposable {
  private val tasksThreadGroup = ThreadGroup("GitToolBox-task-group")
  private val executor = ThreadPoolExecutor(
    1,
    Int.MAX_VALUE,
    60L,
    TimeUnit.SECONDS,
    SynchronousQueue<Runnable>(),
    ThreadFactoryBuilder()
      .setNameFormat("GitToolBox-task-%d")
      .setDaemon(true)
      .setThreadFactory(ThreadGroupThreadFactory(tasksThreadGroup))
      .build()
  )
  private val asyncThreadGroup = ThreadGroup("GitToolBox-async-group")
  private val asyncExecutor = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors(),
    ThreadFactoryBuilder()
      .setNameFormat("GitToolBox-async-%d")
      .setDaemon(true)
      .setThreadFactory(ThreadGroupThreadFactory(asyncThreadGroup))
      .build()
  )
  private val schedulesThreadGroup = ThreadGroup("GitToolBox-schedule-group")
  private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor(
    ThreadFactoryBuilder()
      .setNameFormat("GitToolBox-schedule-%d")
      .setDaemon(true)
      .setThreadFactory(ThreadGroupThreadFactory(schedulesThreadGroup))
      .build()
  )
  private val active = AtomicBoolean(true)

  init {
    val metrics = AppMetrics.getInstance()
    ThreadGroupMetrics.expose(tasksThreadGroup, metrics)
    ThreadGroupMetrics.expose(asyncThreadGroup, metrics)
    ThreadGroupMetrics.expose(schedulesThreadGroup, metrics)
  }

  override fun dispose() {
    if (active.compareAndSet(true, false)) {
      ConcurrentUtil.shutdown(scheduledExecutor)
      ConcurrentUtil.shutdown(executor)
      ConcurrentUtil.shutdown(asyncExecutor)
    }
  }

  fun runInBackground(task: Runnable) {
    if (active.get()) {
      executor.submit(task)
    }
  }

  fun <T> supplyAsyncList(supplier: Supplier<List<T>>): CompletableFuture<List<T>> {
    return if (active.get()) {
      CompletableFuture.supplyAsync(supplier, asyncExecutor)
    } else {
      CompletableFuture.completedFuture(Collections.emptyList())
    }
  }

  fun schedule(task: Runnable, delay: Long, unit: TimeUnit) {
    if (active.get()) {
      scheduledExecutor.schedule({ runInBackground(task) }, delay, unit)
    }
  }

  fun publishSync(project: Project, publisher: (messageBus: MessageBus) -> Unit) {
    if (active.get()) {
      publisher.invoke(project.messageBus)
    }
  }

  companion object {
    @JvmStatic
    fun getInstance(): Optional<GitToolBoxApp> {
      return AppUtil.getServiceInstanceSafe(GitToolBoxApp::class.java)
    }
  }
}

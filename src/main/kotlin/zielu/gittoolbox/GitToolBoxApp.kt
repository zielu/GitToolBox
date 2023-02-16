package zielu.gittoolbox

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBus
import zielu.gittoolbox.metrics.AppMetrics
import zielu.gittoolbox.util.AppUtil
import zielu.gittoolbox.util.ConcurrentUtil
import zielu.gittoolbox.util.ReschedulingExecutor
import zielu.intellij.concurrent.ZThreadFactory
import java.util.Collections
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier

internal class GitToolBoxApp : Disposable {
  private val taskThreadFactory = ZThreadFactory("GitToolBox-task-group")
  private val taskExecutor = ThreadPoolExecutor(
    1,
    1024,
    60L,
    TimeUnit.SECONDS,
    SynchronousQueue(),
    ThreadFactoryBuilder()
      .setNameFormat("GitToolBox-task-%d")
      .setDaemon(true)
      .setThreadFactory(taskThreadFactory)
      .build()
  )
  private val asyncThreadFactory = ZThreadFactory("GitToolBox-async-group")
  private val asyncExecutor = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors(),
    ThreadFactoryBuilder()
      .setNameFormat("GitToolBox-async-%d")
      .setDaemon(true)
      .setThreadFactory(asyncThreadFactory)
      .build()
  )
  private val scheduledThreadFactory = ZThreadFactory("GitToolBox-schedule-group")
  private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor(
    ThreadFactoryBuilder()
      .setNameFormat("GitToolBox-schedule-%d")
      .setDaemon(true)
      .setThreadFactory(scheduledThreadFactory)
      .build()
  )
  private val active = AtomicBoolean(true)

  init {
    val metrics = AppMetrics.getInstance()
    taskThreadFactory.exposeMetrics(metrics)
    asyncThreadFactory.exposeMetrics(metrics)
    scheduledThreadFactory.exposeMetrics(metrics)
    runInBackground { AppMetrics.startReporting() }
  }

  override fun dispose() {
    if (active.compareAndSet(true, false)) {
      ConcurrentUtil.shutdown(scheduledExecutor)
      ConcurrentUtil.shutdown(taskExecutor)
      ConcurrentUtil.shutdown(asyncExecutor)
    }
  }

  fun runInBackground(task: Runnable) {
    if (active.get()) {
      taskExecutor.submit(task)
    }
  }

  fun <T> supplyAsyncList(supplier: Supplier<List<T>>): CompletableFuture<List<T>> {
    return if (active.get()) {
      CompletableFuture.supplyAsync(supplier, asyncExecutor)
    } else {
      CompletableFuture.completedFuture(Collections.emptyList())
    }
  }

  fun <T> supplyAsync(supplier: Supplier<T>, fallbackSupplier: Supplier<T>): CompletableFuture<T> {
    return if (active.get()) {
      CompletableFuture.supplyAsync(supplier, asyncExecutor)
    } else {
      CompletableFuture.completedFuture(fallbackSupplier.get())
    }
  }

  fun asyncExecutor(): Executor = asyncExecutor

  fun schedule(task: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*>? {
    return if (active.get()) {
      scheduledExecutor.schedule({ runInBackground(task) }, delay, unit)
    } else {
      null
    }
  }

  fun publishSync(project: Project, publisher: (messageBus: MessageBus) -> Unit) {
    if (active.get()) {
      publisher.invoke(project.messageBus)
    }
  }

  fun publishSync(publisher: (messageBus: MessageBus) -> Unit) {
    if (active.get()) {
      publisher.invoke(ApplicationManager.getApplication().messageBus)
    }
  }

  companion object {
    @JvmStatic
    fun getInstance(): Optional<GitToolBoxApp> {
      return AppUtil.getServiceInstanceSafe(GitToolBoxApp::class.java)
    }

    @JvmStatic
    fun createReschedulingExecutor(): ReschedulingExecutor {
      return ReschedulingExecutor(
        { task, duration ->
          getInstance().flatMap {
            val result: Optional<Future<*>> = Optional.ofNullable(
              it.schedule(task, duration.toMillis(), TimeUnit.MILLISECONDS)
            )
            result
          }
        },
        true
      )
    }
  }
}

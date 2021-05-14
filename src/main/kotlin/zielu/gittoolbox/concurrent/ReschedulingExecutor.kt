package zielu.gittoolbox.concurrent

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Disposer
import zielu.gittoolbox.GitToolBoxApp
import zielu.intellij.concurrent.ZDisposableRunnableWrapper
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class ReschedulingExecutor : Disposable {
  private val lock = ReentrantLock()
  private val active = AtomicBoolean(true)
  private val tasks: MutableMap<String, ScheduledFuture<*>> = HashMap()

  fun scheduleOnce(id: String, task: Runnable, delay: Long, timeUnit: TimeUnit): Future<*>? {
    if (!active.get()) {
      return null
    }

    lock.withLock {
      var finalDelayMillis = TimeUnit.MILLISECONDS.convert(delay, timeUnit)
      val oldTask = tasks[id]
      if (oldTask != null) {
        finalDelayMillis = oldTaskExists(oldTask, delay, timeUnit)
        oldTask.cancel(true)
        log.debug("Cancelled $id: $oldTask")
      }
      val wrappedTask = ZDisposableRunnableWrapper(task)
      Disposer.register(this, wrappedTask)
      val scheduled = GitToolBoxApp.getInstance().map {
        it.schedule(wrappedTask, finalDelayMillis, TimeUnit.MILLISECONDS)
      }.orElse(null)
      log.debug("Scheduled $id in $finalDelayMillis ms: $scheduled")
      scheduled?.apply { tasks[id] = this }
      return scheduled
    }
  }

  private fun oldTaskExists(oldTask: ScheduledFuture<*>, delay: Long, timeUnit: TimeUnit): Long {
    val timeLeftMillis = oldTask.getDelay(TimeUnit.MILLISECONDS)
    val delayMillis = TimeUnit.MILLISECONDS.convert(delay, timeUnit)
    return if (timeLeftMillis < delayMillis) {
      if (taskExpired(oldTask)) {
        delayMillis
      } else {
        timeLeftMillis
      }
    } else {
      delayMillis
    }
  }

  private fun taskExpired(task: ScheduledFuture<*>): Boolean {
    return task.isDone || task.isCancelled
  }


  override fun dispose() {
    if (active.compareAndSet(true, false)) {
      lock.withLock {
        tasks.values.forEach { it.cancel(true) }
        tasks.clear()
      }
    }
  }

  private companion object {
    private val log = Logger.getInstance(ReschedulingExecutor::class.java)
  }
}

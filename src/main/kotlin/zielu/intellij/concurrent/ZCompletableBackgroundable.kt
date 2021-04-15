package zielu.intellij.concurrent

import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor

internal abstract class ZCompletableBackgroundable<T>(
  project: Project?,
  title: String,
  canBeCancelled: Boolean = true
) : Task.Backgroundable(
  project,
  title,
  canBeCancelled
) {
  private val latch = CountDownLatch(1)
  private var result: T? = null
  private var error: Throwable? = null
  private var cancelled: Boolean = false

  protected abstract fun getResult(): T

  final override fun onCancel() {
    cancelled = true
    latch.countDown()
  }

  final override fun onSuccess() {
    result = getResult()
    latch.countDown()
  }

  final override fun onThrowable(error: Throwable) {
    this.error = error
    latch.countDown()
  }

  private fun supplyAsyncResult(): T {
    latch.await()
    error?.apply { throw this }
    return result!!
  }

  fun asCompletableFuture(executor: Executor): CompletableFuture<T> {
    queue()
    return CompletableFuture.supplyAsync({ supplyAsyncResult() }, executor)
  }
}

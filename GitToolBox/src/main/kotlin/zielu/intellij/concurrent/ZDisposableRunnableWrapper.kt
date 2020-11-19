package zielu.intellij.concurrent

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Disposer
import zielu.intellij.util.ZDisposeGuard
import java.util.concurrent.atomic.AtomicReference

internal class ZDisposableRunnableWrapper(private val task: Runnable) : Runnable, Disposable {
  private val thread = AtomicReference<Thread>()
  private val disposeGuard = ZDisposeGuard()

  override fun run() {
    try {
      if (disposeGuard.isActive()) {
        thread.set(Thread.currentThread())
        task.run()
      }
    } finally {
      thread.set(null)
      Disposer.dispose(this)
      log.debug("Disposed after ", task, " was done")
    }
  }

  override fun dispose() {
    thread.getAndSet(null)?.interrupt()
    Disposer.dispose(disposeGuard)
  }

  private companion object {
    private val log = Logger.getInstance(ZDisposableRunnableWrapper::class.java)
  }
}

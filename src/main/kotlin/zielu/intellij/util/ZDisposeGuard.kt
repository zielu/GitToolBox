package zielu.intellij.util

import com.intellij.openapi.Disposable
import java.util.concurrent.atomic.AtomicBoolean

internal class ZDisposeGuard : Disposable {
  private val disposed = AtomicBoolean(false)

  fun checkAndThrow() {
    if (disposed.get() || checkInterrupted()) {
      throw ZDisposedException("Disposed")
    }
  }

  fun isActive(): Boolean = !disposed.get() && !checkInterrupted()

  private fun checkInterrupted(): Boolean = Thread.currentThread().isInterrupted

  fun ifActive(action: () -> Unit) {
    if (isActive()) {
      action.invoke()
    }
  }

  override fun dispose() {
    disposed.compareAndSet(false, true)
  }
}

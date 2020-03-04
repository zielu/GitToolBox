package zielu.intellij.util

import java.util.concurrent.atomic.AtomicBoolean

internal fun createBooleanProperty(): ZProperty<Boolean> {
  return ZBoolPropertyImpl()
}

private class ZBoolPropertyImpl : ZProperty<Boolean> {
  private val value: AtomicBoolean = AtomicBoolean()

  override fun get(): Boolean = value.get()

  override fun set(value: Boolean) {
    this.value.set(value)
  }
}

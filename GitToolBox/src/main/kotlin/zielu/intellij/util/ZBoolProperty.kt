package zielu.intellij.util

import java.util.concurrent.atomic.AtomicBoolean

internal interface ZBoolProperty {
  fun get(): Boolean
  fun set(value: Boolean)
}

internal fun createZBoolProperty(): ZBoolProperty {
  return ZBoolPropertyImpl()
}

private class ZBoolPropertyImpl : ZBoolProperty {
  private val value: AtomicBoolean = AtomicBoolean()

  override fun get(): Boolean = value.get()

  override fun set(value: Boolean) {
    this.value.set(value)
  }
}

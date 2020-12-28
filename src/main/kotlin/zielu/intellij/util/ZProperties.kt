package zielu.intellij.util

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

internal fun <T> createRefProperty(initialValue: T): ZProperty<T> {
  return ZRefPropertyImpl(initialValue)
}

internal fun createIntProperty(initialValue: Int): ZProperty<Int> {
  return ZIntPropertyImpl(initialValue)
}

private class ZRefPropertyImpl<T>(
  initialValue: T
) : ZProperty<T> {
  private val valueStore: AtomicReference<T> = AtomicReference(initialValue)

  override var value: T
    get() = valueStore.get()
    set(value) {
      valueStore.set(value)
    }
}

private class ZIntPropertyImpl(
  initialValue: Int
) : ZProperty<Int> {
  private val valueStore = AtomicInteger(initialValue)

  override var value: Int
    get() = valueStore.get()
    set(value) {
      valueStore.set(value)
    }
}

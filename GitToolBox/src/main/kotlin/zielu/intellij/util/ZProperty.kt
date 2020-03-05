package zielu.intellij.util

internal interface ZProperty<T> {
  fun get(): T
  fun set(value: T)
}

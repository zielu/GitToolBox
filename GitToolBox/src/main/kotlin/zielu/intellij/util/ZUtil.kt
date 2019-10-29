package zielu.intellij.util

object ZUtil {
  @JvmStatic
  fun <T> defaultIfNull(value: T, defaultSupplier: () -> T): T {
    return value ?: defaultSupplier.invoke()
  }
}

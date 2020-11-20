package zielu.intellij.metrics

import java.util.concurrent.TimeUnit
import java.util.function.Supplier

internal interface GtTimer {
  fun <T> timeSupplierKt(function: () -> T): T
  fun <T> timeSupplier(function: Supplier<T>): T
  fun timeKt(function: () -> Unit)
  fun time(function: Runnable)
  fun update(duration: Long, unit: TimeUnit)
}

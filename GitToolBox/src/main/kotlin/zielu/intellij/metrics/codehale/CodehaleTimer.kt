package zielu.intellij.metrics.codehale

import com.codahale.metrics.Timer
import zielu.intellij.metrics.GtTimer
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

internal class CodehaleTimer(private val delegate: Timer) : GtTimer {
  override fun <T> timeSupplierKt(function: () -> T): T {
    return delegate.timeSupplier(function)
  }

  override fun <T> timeSupplier(function: Supplier<T>): T {
    return delegate.timeSupplier(function)
  }

  override fun timeKt(function: () -> Unit) {
    delegate.time(function)
  }

  override fun time(function: Runnable) {
    delegate.time(function)
  }

  override fun update(duration: Long, unit: TimeUnit) {
    delegate.update(duration, unit)
  }
}

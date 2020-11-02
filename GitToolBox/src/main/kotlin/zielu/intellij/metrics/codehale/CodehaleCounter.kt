package zielu.intellij.metrics.codehale

import com.codahale.metrics.Counter
import zielu.intellij.metrics.GtCounter

internal class CodehaleCounter(private val delegate: Counter) : GtCounter {
  override fun inc() {
    delegate.inc()
  }

  override fun dec() {
    delegate.dec()
  }
}

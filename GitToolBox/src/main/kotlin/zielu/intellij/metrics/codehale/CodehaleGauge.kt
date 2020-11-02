package zielu.intellij.metrics.codehale

import com.codahale.metrics.Gauge
import zielu.intellij.metrics.GtGauge

internal class CodehaleGauge(delegate: Gauge<*>) : GtGauge

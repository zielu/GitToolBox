package zielu.intellij.log

import com.intellij.openapi.diagnostic.Logger
import jodd.util.StringBand

internal fun Logger.info(message: String, vararg details: Any?) {
  val builder = StringBand(message)
  details.forEach { builder.append(it) }
  info(builder.toString())
}

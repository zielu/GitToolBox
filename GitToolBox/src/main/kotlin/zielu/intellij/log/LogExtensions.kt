package zielu.intellij.log

import com.intellij.openapi.diagnostic.Logger

fun Logger.info(message: String, vararg details: Any?) {
  val builder = StringBuilder(message)
  details.forEach { builder.append(it) }
  info(builder.toString())
}

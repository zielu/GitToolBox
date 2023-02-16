package zielu.gittoolbox.formatter

import org.apache.commons.lang3.StringUtils

internal data class Formatted(
  val text: String,
  val matches: Boolean,
  val message: String? = null
) {
  fun getDisplayable() = matches && StringUtils.isNotBlank(text)
}

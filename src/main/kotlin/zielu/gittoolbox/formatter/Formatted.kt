package zielu.gittoolbox.formatter

import org.apache.commons.lang3.StringUtils

internal data class Formatted(
  val text: String,
  val matches: Boolean
) {
  fun getDisplayable() = matches && StringUtils.isNotBlank(text)
}

package zielu.gittoolbox.formatter

import zielu.gittoolbox.IconHandle

internal object SimpleFormatter : Formatter {
  override fun getIconHandle() = IconHandle.SIMPLE_FORMATTER

  override fun format(input: String) = Formatted(input, true)
}

package zielu.gittoolbox.formatter

import zielu.gittoolbox.completion.FormatterIcons

internal object SimpleFormatter : Formatter {
  override fun getIcon() = FormatterIcons.Simple

  override fun format(input: String) = Formatted(input, true)
}

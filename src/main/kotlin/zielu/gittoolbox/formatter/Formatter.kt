package zielu.gittoolbox.formatter

import javax.swing.Icon

internal interface Formatter {
  fun format(input: String): Formatted
  fun getIcon(): Icon
}

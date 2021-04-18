package zielu.gittoolbox.formatter

import jodd.util.StringBand
import zielu.gittoolbox.completion.FormatterIcons
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.swing.Icon

internal class RegExpFormatter(
  private val pattern: Pattern?
) : Formatter {

  override fun format(input: String): Formatted {
    return if (pattern == null) {
      Formatted(input, false, "no pattern")
    } else {
      val matcher = pattern.matcher(input)
      return if (matcher.matches()) {
        Formatted(format(matcher), true, null)
      } else {
        Formatted(input, false, "pattern: ${pattern.pattern()}")
      }
    }
  }

  private fun format(matcher: Matcher): String {
    val count = matcher.groupCount()
    val result = StringBand(count)
    for (i in 1..count) {
      result.append(matcher.group(i))
    }
    return result.toString()
  }

  override fun getIcon(): Icon = FormatterIcons.RegExp

  companion object {
    private val empty = RegExpFormatter(null)

    @JvmStatic
    fun create(pattern: String?): Formatter {
      return if (pattern != null && pattern.isNotBlank()) {
        try {
          RegExpFormatter(Pattern.compile(pattern))
        } catch (e: PatternSyntaxException) {
          empty
        }
      } else {
        empty
      }
    }
  }
}

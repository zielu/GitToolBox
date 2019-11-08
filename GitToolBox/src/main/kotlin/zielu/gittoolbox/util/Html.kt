package zielu.gittoolbox.util

object Html {
  const val BRX = "<br/>"
  const val BR = "<br>"
  const val HRX = "<hr/>"
  const val LT = "&lt;"
  const val GT = "&gt;"
  private const val NBSP = "&nbsp;"

  @JvmStatic
  fun br(count: Int): String {
    return BR.repeat(count)
  }

  @JvmStatic
  fun nbsp(count: Int): String {
    return NBSP.repeat(count)
  }

  @JvmStatic
  fun link(name: String, text: String): String {
    return "<a href=\"$name\">$text</a>"
  }

  @JvmStatic
  fun underline(text: String): String {
    return surround(text, "underline")
  }

  private fun surround(text: String, tag: String): String {
    return "<$tag>$text</$tag>"
  }
}

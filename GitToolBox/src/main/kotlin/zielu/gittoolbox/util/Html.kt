package zielu.gittoolbox.util

object Html {
  const val BR = "<br/>"
  const val HR = "<hr/>"

  @JvmStatic
  fun link(name: String, text: String): String? {
    return "<a href=\"$name\">$text</a>"
  }

  @JvmStatic
  fun underline(text: String): String? {
    return surround(text, "underline")
  }

  private fun surround(text: String, tag: String): String? {
    return "<$tag>$text</$tag>"
  }
}

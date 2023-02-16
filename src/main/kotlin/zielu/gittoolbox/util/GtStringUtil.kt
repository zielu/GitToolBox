package zielu.gittoolbox.util

internal object GtStringUtil {
  @JvmStatic
  fun firstLine(input: String?): String? {
    return input?.let { findFirstLine(it) }
  }

  private fun findFirstLine(input: String): String {
    var i = 0
    val n = input.length
    while (i < n) {
      val codePointAt = input.codePointAt(i)
      if (codePointAt == '\n'.toInt() || codePointAt == '\r'.toInt()) {
        return input.substring(0, i)
      }
      i++
    }
    return input
  }
}

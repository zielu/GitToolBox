package zielu.gittoolbox

internal object UtfSeq {
  const val ARROW_UP = "\u2191"
  const val ARROW_DOWN = "\u2193"
  const val ARROW_RIGHT = "\u2192"
  const val ARROWHEAD_UP = "\u02C4"
  const val ARROWHEAD_DOWN = "\u02C5"
  const val ARROWHEAD_LEFT = "\u02C2"
  const val ARROWHEAD_RIGHT = "\u02C3"
  const val INCREMENT = "\u2206"
  const val BULLET = "\u2022"
  const val DELTA = "\u0394"
  const val EMPTY_SET = "\u2205"

  private val variationChars: List<Char> by lazy {
    Character.toChars(Integer.parseInt("FE0F", 16)).toList()
  }

  fun fromCodepoint(codePoint: String, variation: Boolean): List<Char> {
    val pointInt = Integer.parseInt(codePoint, 16)
    val chars = Character.toChars(pointInt)
    return if (variation) {
        val basic = chars.toMutableList()
        basic.addAll(variationChars)
      basic.toList()
    } else {
      chars.toList()
    }
  }
}

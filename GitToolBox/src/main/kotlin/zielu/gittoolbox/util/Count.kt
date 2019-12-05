package zielu.gittoolbox.util

internal data class Count(
  val value: Int
) {

  fun isEmpty(): Boolean {
    return value == Int.MIN_VALUE
  }

  companion object {
    @JvmField
    val EMPTY: Count = Count(Int.MIN_VALUE)

    @JvmField
    val ZERO: Count = Count(0)
  }
}

package zielu.gittoolbox.status

import java.util.OptionalInt

internal data class BehindStatus(
  val behind: Int,
  val status: Status,
  val delta: Int?
) {
  constructor(revListCount: RevListCount) : this(revListCount.value!!, revListCount.status, null)

  constructor(revListCount: RevListCount, delta: Int) : this(revListCount.value!!, revListCount.status, delta)

  constructor(count: Int, delta: Int) : this(count, Status.SUCCESS, delta)

  constructor(count: Int) : this(count, Status.SUCCESS, null)

  @Deprecated("replace with Kotlin syntax", replaceWith = ReplaceWith("delta?."))
  fun delta(): OptionalInt {
    return delta?.let { OptionalInt.of(it) } ?: OptionalInt.empty()
  }

  companion object {
    private val empty = BehindStatus(0)

    @JvmStatic
    fun empty(): BehindStatus = empty
  }
}

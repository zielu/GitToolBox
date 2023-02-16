package zielu.gittoolbox.status

import com.intellij.vcs.log.Hash
import java.util.OptionalInt

internal data class RevListCount(
  val status: Status,
  val top: Hash?,
  val value: Int?
) {
  constructor(top: Hash?, value: Int) : this(Status.SUCCESS, top, value)

  fun value(): OptionalInt {
    return value?.let { OptionalInt.of(value) } ?: OptionalInt.empty()
  }

  companion object {
    private val noRemote = RevListCount(Status.NO_REMOTE, null, null)
    private val cancel = RevListCount(Status.CANCEL, null, null)
    private val failure = RevListCount(Status.FAILURE, null, null)

    @JvmStatic
    fun noRemote(): RevListCount = noRemote

    fun cancel(): RevListCount = cancel

    fun failure(): RevListCount = failure
  }
}

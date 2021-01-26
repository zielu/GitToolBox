package zielu.gittoolbox.status

import com.intellij.vcs.log.Hash

internal data class GitAheadBehindCount constructor(
  val ahead: RevListCount,
  val behind: RevListCount
) {

  fun status(): Status = ahead.status()

  fun isNotZero(): Boolean {
    return if (status() == Status.SUCCESS) {
      ahead.value() != 0 || behind.value() != 0
    } else {
      false
    }
  }

  fun isNotZeroBehind(): Boolean {
    return if (status() == Status.SUCCESS) {
      behind.value() != 0
    } else {
      false
    }
  }

  companion object {
    private val cancel = GitAheadBehindCount(RevListCount.cancel(), RevListCount.cancel())
    private val failure = GitAheadBehindCount(RevListCount.failure(), RevListCount.failure())
    private val noRemote = GitAheadBehindCount(RevListCount.noRemote(), RevListCount.noRemote())

    @JvmStatic
    fun success(
      ahead: Int,
      aheadHash: Hash?,
      behind: Int,
      behindHash: Hash?
    ): GitAheadBehindCount {
      return GitAheadBehindCount(RevListCount.success(ahead, aheadHash), RevListCount.success(behind, behindHash))
    }

    @JvmStatic
    fun cancel(): GitAheadBehindCount = cancel

    @JvmStatic
    fun failure(): GitAheadBehindCount = failure

    @JvmStatic
    fun noRemote(): GitAheadBehindCount = noRemote
  }
}

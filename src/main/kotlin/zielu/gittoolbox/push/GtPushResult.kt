package zielu.gittoolbox.push

internal data class GtPushResult(
  val type: PushResultType,
  val output: String,
  val rejectedBranches: List<String> = listOf()
) {

  internal companion object {
    private val success = GtPushResult(PushResultType.SUCCESS, "")
    private val cancelled = GtPushResult(PushResultType.CANCELLED, "")

    @JvmStatic
    fun success(): GtPushResult = success

    @JvmStatic
    fun cancelled(): GtPushResult = cancelled

    @JvmStatic
    fun error(output: String): GtPushResult {
      return GtPushResult(PushResultType.ERROR, output)
    }

    @JvmStatic
    fun rejected(branches: Collection<String>): GtPushResult {
      return GtPushResult(PushResultType.REJECTED, "", branches.toList())
    }
  }
}

internal enum class PushResultType {
  SUCCESS, REJECTED, ERROR, CANCELLED, NOT_AUTHORIZED
}

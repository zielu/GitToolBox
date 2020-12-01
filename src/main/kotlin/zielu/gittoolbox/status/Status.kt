package zielu.gittoolbox.status

internal enum class Status(private val valid: Boolean) {
  SUCCESS(true),
  NO_REMOTE(true),
  CANCEL(false),
  FAILURE(false);

  fun isValid() = valid
}

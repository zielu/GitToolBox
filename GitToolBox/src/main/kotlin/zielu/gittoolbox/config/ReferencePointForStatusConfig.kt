package zielu.gittoolbox.config

internal data class ReferencePointForStatusConfig(
  val type: ReferencePointForStatusType = ReferencePointForStatusType.AUTOMATIC,
  val name: String = ""
) {
  fun copy(): ReferencePointForStatusConfig {
    return ReferencePointForStatusConfig(type, name)
  }
}

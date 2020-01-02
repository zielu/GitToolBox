package zielu.gittoolbox.config

internal data class ReferencePointForStatusConfig(
  var type: ReferencePointForStatusType = ReferencePointForStatusType.AUTOMATIC,
  var name: String = ""
) {
  fun copy(): ReferencePointForStatusConfig {
    return ReferencePointForStatusConfig(type, name)
  }
}

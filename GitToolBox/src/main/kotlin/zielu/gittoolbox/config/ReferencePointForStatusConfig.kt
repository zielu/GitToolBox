package zielu.gittoolbox.config

import com.intellij.util.xmlb.annotations.Transient

internal data class ReferencePointForStatusConfig(
  var type: ReferencePointForStatusType = ReferencePointForStatusType.AUTOMATIC,
  var name: String = ""
) {

  @Transient
  fun copy(): ReferencePointForStatusConfig {
    return ReferencePointForStatusConfig(type, name)
  }
}

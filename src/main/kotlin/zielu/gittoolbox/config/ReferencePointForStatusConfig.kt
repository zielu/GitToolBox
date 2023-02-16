package zielu.gittoolbox.config

import com.intellij.util.xmlb.annotations.Transient

internal data class ReferencePointForStatusConfig(
  var type: ReferencePointForStatusType = ReferencePointForStatusType.AUTOMATIC,
  var name: String = ""
) : ConfigItem<ReferencePointForStatusConfig> {

  @Transient
  override fun copy(): ReferencePointForStatusConfig {
    return ReferencePointForStatusConfig(type, name)
  }
}

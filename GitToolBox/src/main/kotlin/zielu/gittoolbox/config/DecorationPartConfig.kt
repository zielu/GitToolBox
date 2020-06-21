package zielu.gittoolbox.config

import com.intellij.util.xmlb.annotations.Transient

internal data class DecorationPartConfig(
  val type: DecorationPartType,
  val prefix: String,
  val postfix: String
) {
    constructor(type: DecorationPartType) : this(type, "", "")

    constructor(type: DecorationPartType, prefix: String) : this(type, prefix, "")

    @Transient
    fun copy(): DecorationPartConfig {
        return DecorationPartConfig(
          type,
          prefix,
          postfix
        )
    }
}

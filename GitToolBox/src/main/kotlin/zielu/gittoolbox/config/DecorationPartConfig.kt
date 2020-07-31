package zielu.gittoolbox.config

import com.intellij.util.xmlb.annotations.Transient

internal data class DecorationPartConfig(
  var type: DecorationPartType,
  var prefix: String,
  var postfix: String
) {
    constructor() : this(DecorationPartType.UNKNOWN)

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

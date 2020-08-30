package zielu.gittoolbox.blame.persistence

import com.intellij.util.xmlb.annotations.XMap

internal data class BlameState(
  @XMap(
    keyAttributeName = "fileUrl"
  )
  var fileBlames: MutableMap<String, FileBlameState> = mutableMapOf()
)

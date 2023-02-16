package zielu.gittoolbox.blame.calculator.persistence

import com.intellij.util.xmlb.annotations.XMap

internal data class BlameState(
  @XMap(
    keyAttributeName = "fileUrl"
  )
  var fileBlames: Map<String, FileBlameState> = mapOf()
)

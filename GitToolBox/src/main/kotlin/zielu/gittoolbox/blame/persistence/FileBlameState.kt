package zielu.gittoolbox.blame.persistence

import com.intellij.util.xmlb.annotations.XCollection

internal data class FileBlameState(
  var accessTimestamp: Long = 0,
  var revision: BlameRevisionState = BlameRevisionState(),
  @XCollection(
    propertyElementName = "lines",
    style = XCollection.Style.v2
  )
  var lines: List<LineState> = listOf()
)

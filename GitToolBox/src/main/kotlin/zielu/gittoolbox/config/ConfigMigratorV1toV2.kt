package zielu.gittoolbox.config

import com.intellij.openapi.diagnostic.Logger
import java.util.ArrayList

internal class ConfigMigratorV1toV2(
  private val v1: GitToolBoxConfig = GitToolBoxConfig.getInstance()
) {

  fun migrate(v2: GitToolBoxConfig2) {
    if (v1.isVanilla) {
      log.info("V1 config is vanilla, no migration needed")
      return
    }
    v2.presentationMode = v1.presentationMode
    v2.updateProjectActionId = v1.updateProjectActionId
    v2.showStatusWidget = v1.showStatusWidget
    v2.behindTracker = v1.behindTracker
    v2.showProjectViewStatus = v1.showProjectViewStatus
    val decorationParts: MutableList<DecorationPartConfig> = ArrayList()
    decorationParts.add(DecorationPartConfig(DecorationPartType.BRANCH))
    decorationParts.add(DecorationPartConfig(DecorationPartType.STATUS))
    if (v1.showProjectViewHeadTags) {
      val tagsOnHead = DecorationPartConfig(DecorationPartType.TAGS_ON_HEAD, "(", ")")
      decorationParts.add(tagsOnHead)
    }
    if (v1.showProjectViewLocationPath) {
      if (v1.showProjectViewStatusBeforeLocation) {
        decorationParts.add(DecorationPartConfig(DecorationPartType.LOCATION, "- "))
      } else {
        decorationParts.add(0, DecorationPartConfig(DecorationPartType.LOCATION))
      }
    }
    v2.decorationParts = decorationParts
  }

  private companion object {
    private val log = Logger.getInstance(ConfigMigratorV1toV2::class.java)
  }
}

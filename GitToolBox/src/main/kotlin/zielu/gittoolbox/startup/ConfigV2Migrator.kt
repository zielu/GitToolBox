package zielu.gittoolbox.startup

import zielu.gittoolbox.config.AuthorNameType
import zielu.gittoolbox.config.GitToolBoxConfig2

internal class ConfigV2Migrator(private val config: GitToolBoxConfig2) {

  fun migrate(): Boolean {
    if (config.blameInlineAuthorNameType == null) {
      config.blameInlineAuthorNameType = AuthorNameType.LASTNAME
      return true
    }
    return false
  }
}

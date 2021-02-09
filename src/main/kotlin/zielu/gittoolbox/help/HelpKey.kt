package zielu.gittoolbox.help

import zielu.gittoolbox.GitToolBox

internal enum class HelpKey(
  val id: String,
  val anchorId: String
) {
  APP_CONFIG(GitToolBox.PLUGIN_ID + ".appConfig", "global-configuration"),
  PROJECT_CONFIG(GitToolBox.PLUGIN_ID + ".projectConfig", "project-configuration")
  ;

  companion object {
    fun findKeyById(id: String): HelpKey? {
      for (key in values()) {
        if (key.id == id) {
          return key
        }
      }
      return null
    }
  }
}

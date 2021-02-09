package zielu.gittoolbox.help

import com.intellij.openapi.help.WebHelpProvider

internal class GithubWikiHelpProvider : WebHelpProvider() {

  override fun getHelpPageUrl(helpTopicId: String): String {
    val anchor = HelpKey.findKeyById(helpTopicId)?.anchorId?.let { "#$it" } ?: ""
    return "https://github.com/zielu/GitToolBox/wiki/Manual$anchor"
  }
}

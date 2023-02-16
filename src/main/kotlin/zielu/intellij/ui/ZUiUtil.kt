package zielu.intellij.ui

import com.intellij.util.ui.StartupUiUtil
import com.intellij.util.ui.UIUtil

internal object ZUiUtil {
  @JvmStatic
  fun asHtml(content: String): String {
    val contentWithConvertedNewLines = content.replace("\n", "<br>")
    return (
      "<html><head>" + UIUtil.getCssFontDeclaration(StartupUiUtil.getLabelFont()) + "</head><body>" +
        contentWithConvertedNewLines +
        "</body></html>"
      )
  }
}

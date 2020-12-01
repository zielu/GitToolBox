package zielu.gittoolbox.ui.projectview

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.ui.SimpleTextAttributes
import zielu.gittoolbox.config.DecorationColors

internal object DecorationColorsTextAttributesUi : TextAttributesUi {

  @JvmStatic
  fun getInstance(): TextAttributesUi {
    return this
  }

  override fun getTextAttributes(key: TextAttributesKey): SimpleTextAttributes {
    return DecorationColors.simpleAttributes(key)
  }
}

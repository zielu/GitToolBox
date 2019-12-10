package zielu.gittoolbox.config

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.SimpleTextAttributes

internal object DecorationColors {
  @JvmField
  val REMOTE_BRANCH_ATTRIBUTES = TextAttributesKey
    .createTextAttributesKey("GIT_TOOLBOX.REMOTE_BRANCH_ATTRIBUTES")
  @JvmField
  val LOCAL_BRANCH_ATTRIBUTES = TextAttributesKey
    .createTextAttributesKey("GIT_TOOLBOX.LOCAL_BRANCH_ATTRIBUTES")
  @JvmField
  val STATUS_ATTRIBUTES = TextAttributesKey
    .createTextAttributesKey("GIT_TOOLBOX.STATUS_ATTRIBUTES")
  @JvmField
  val HEAD_TAGS_ATTRIBUTES = TextAttributesKey
    .createTextAttributesKey("GIT_TOOLBOX.HEAD_TAGS_ATTRIBUTES")
  @JvmField
  val CHANGED_COUNT_ATTRIBUTES = TextAttributesKey
    .createTextAttributesKey("GIT_TOOLBOX.CHANGED_COUNT_ATTRIBUTES")
  @JvmField
  val EDITOR_INLINE_BLAME_ATTRIBUTES = TextAttributesKey
    .createTextAttributesKey("GIT_TOOLBOX.EDITOR_INLINE_BLAME_ATTRIBUTES")

  @JvmStatic
  fun simpleAttributes(key: TextAttributesKey): SimpleTextAttributes {
    return SimpleTextAttributes.fromTextAttributes(textAttributes(key))
  }

  fun textAttributes(key: TextAttributesKey): TextAttributes {
    val scheme = EditorColorsManager.getInstance().globalScheme
    return scheme.getAttributes(key)
  }
}

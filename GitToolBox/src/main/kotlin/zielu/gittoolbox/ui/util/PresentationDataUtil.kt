package zielu.gittoolbox.ui.util

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.FontUtil

internal object PresentationDataUtil {
  @JvmField
  val wideSpacer = ColoredFragment(FontUtil.spaceAndThinSpace(), SimpleTextAttributes.REGULAR_ATTRIBUTES)
  @JvmField
  val spacer = ColoredFragment(FontUtil.thinSpace(), SimpleTextAttributes.REGULAR_ATTRIBUTES)

  @JvmStatic
  fun hasEmptyColoredTextValue(data: PresentationData): Boolean {
    return data.coloredText.isEmpty()
  }
}

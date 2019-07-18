package zielu.gittoolbox.ui.util;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.FontUtil;

public final class PresentationDataUtil {
  private PresentationDataUtil() {
    throw new IllegalStateException();
  }

  private static final ColoredFragment WIDE_SPACER = new ColoredFragment(FontUtil.spaceAndThinSpace(),
      SimpleTextAttributes.REGULAR_ATTRIBUTES);
  private static final ColoredFragment SPACER = new ColoredFragment(FontUtil.thinSpace(),
      SimpleTextAttributes.REGULAR_ATTRIBUTES);

  public static boolean hasEmptyColoredTextValue(PresentationData data) {
    return data.getColoredText().isEmpty();
  }

  public static ColoredFragment wideSpacer() {
    return WIDE_SPACER;
  }

  public static ColoredFragment spacer() {
    return SPACER;
  }
}

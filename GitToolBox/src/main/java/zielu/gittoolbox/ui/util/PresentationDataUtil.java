package zielu.gittoolbox.ui.util;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.FontUtil;
import java.util.Optional;

public final class PresentationDataUtil {
  private PresentationDataUtil() {
    throw new IllegalStateException();
  }

  private static final ColoredFragment SPACER = new ColoredFragment(FontUtil.spaceAndThinSpace(),
      SimpleTextAttributes.REGULAR_ATTRIBUTES);

  public static Optional<String> getFirstColoredTextValue(PresentationData data) {
    return data.getColoredText().stream()
        .map(ColoredFragment::getText)
        .findFirst();
  }

  public static ColoredFragment spacer() {
    return SPACER;
  }
}

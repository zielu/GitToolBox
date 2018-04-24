package zielu.gittoolbox.ui.util;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import java.util.Optional;

public final class PresentationDataUtil {
  private PresentationDataUtil() {
    throw new IllegalStateException();
  }

  public static Optional<String> getFirstColoredTextValue(PresentationData data) {
    return data.getColoredText().stream()
        .map(PresentableNodeDescriptor.ColoredFragment::getText)
        .findFirst();
  }
}

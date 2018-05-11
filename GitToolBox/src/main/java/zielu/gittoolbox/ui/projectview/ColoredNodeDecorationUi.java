package zielu.gittoolbox.ui.projectview;

import static zielu.gittoolbox.config.DecorationColors.LOCAL_BRANCH_ATTRIBUTES;

import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.DecorationColors;
import zielu.gittoolbox.config.GitToolBoxConfig;

public class ColoredNodeDecorationUi extends NodeDecorationUi {
  public ColoredNodeDecorationUi(@NotNull GitToolBoxConfig config) {
    super(config);
  }

  public SimpleTextAttributes getStatusFragmentAttributes() {
    return DecorationColors.simpleAttributes(LOCAL_BRANCH_ATTRIBUTES);
  }
}

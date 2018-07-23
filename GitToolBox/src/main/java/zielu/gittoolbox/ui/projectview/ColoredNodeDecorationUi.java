package zielu.gittoolbox.ui.projectview;

import static zielu.gittoolbox.config.DecorationColors.LOCAL_BRANCH_ATTRIBUTES;
import static zielu.gittoolbox.config.DecorationColors.REMOTE_BRANCH_ATTRIBUTES;
import static zielu.gittoolbox.config.DecorationColors.TAGS_ATTRIBUTES;

import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.DecorationColors;
import zielu.gittoolbox.config.GitToolBoxConfig;

public class ColoredNodeDecorationUi extends NodeDecorationUi {
  public ColoredNodeDecorationUi(@NotNull GitToolBoxConfig config) {
    super(config);
  }

  public SimpleTextAttributes getRemoteBranchStatusAttributes() {
    return DecorationColors.simpleAttributes(REMOTE_BRANCH_ATTRIBUTES);
  }

  public SimpleTextAttributes getLocalBranchStatusAttributes() {
    return DecorationColors.simpleAttributes(LOCAL_BRANCH_ATTRIBUTES);
  }

  public SimpleTextAttributes getTagsAttributes() {
    return DecorationColors.simpleAttributes(TAGS_ATTRIBUTES);
  }
}

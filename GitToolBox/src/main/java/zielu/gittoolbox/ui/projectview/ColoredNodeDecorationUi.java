package zielu.gittoolbox.ui.projectview;

import static zielu.gittoolbox.config.DecorationColors.CHANGED_COUNT_ATTRIBUTES;
import static zielu.gittoolbox.config.DecorationColors.HEAD_TAGS_ATTRIBUTES;
import static zielu.gittoolbox.config.DecorationColors.LOCAL_BRANCH_ATTRIBUTES;
import static zielu.gittoolbox.config.DecorationColors.MASTER_LOCAL_ATTRIBUTES;
import static zielu.gittoolbox.config.DecorationColors.MASTER_WITH_REMOTE_ATTRIBUTES;
import static zielu.gittoolbox.config.DecorationColors.REMOTE_BRANCH_ATTRIBUTES;
import static zielu.gittoolbox.config.DecorationColors.STATUS_ATTRIBUTES;

import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfig2;

class ColoredNodeDecorationUi extends NodeDecorationUi {
  private final TextAttributesUi attributesUi;

  ColoredNodeDecorationUi(@NotNull GitToolBoxConfig2 config, @NotNull TextAttributesUi attributesUi) {
    super(config);
    this.attributesUi = attributesUi;
  }

  SimpleTextAttributes getRemoteBranchAttributes() {
    return attributesUi.getTextAttributes(REMOTE_BRANCH_ATTRIBUTES);
  }

  SimpleTextAttributes getLocalBranchAttributes() {
    return attributesUi.getTextAttributes(LOCAL_BRANCH_ATTRIBUTES);
  }

  SimpleTextAttributes getMasterLocalAttributes() {
    return attributesUi.getTextAttributes(MASTER_LOCAL_ATTRIBUTES);
  }

  SimpleTextAttributes getMasterWithRemoteAttributes() {
    return attributesUi.getTextAttributes(MASTER_WITH_REMOTE_ATTRIBUTES);
  }

  SimpleTextAttributes getHeadTagsAttributes() {
    return attributesUi.getTextAttributes(HEAD_TAGS_ATTRIBUTES);
  }

  SimpleTextAttributes getChangedCountAttributes() {
    return attributesUi.getTextAttributes(CHANGED_COUNT_ATTRIBUTES);
  }

  SimpleTextAttributes getStatusAttributes() {
    return attributesUi.getTextAttributes(STATUS_ATTRIBUTES);
  }

  SimpleTextAttributes getLocationAttributes() {
    return SimpleTextAttributes.GRAY_ATTRIBUTES;
  }

  SimpleTextAttributes getNameAttributes() {
    return SimpleTextAttributes.REGULAR_ATTRIBUTES;
  }
}

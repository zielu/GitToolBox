package zielu.gittoolbox.ui.config;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import java.util.Map;
import javax.swing.Icon;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.config.DecorationColors;

public class DecorationColorsPage implements ColorSettingsPage {
  private static final String LOCAL_BRANCH_DEMO_TEXT = new StringBand()
      .append("<localBranch>Local_Branch</localBranch>")
      .append(" <headTags>1.0.0, 1.1.0</headTags>")
      .append(" <changedCount>3 changes</changedCount>")
      .toString();

  private static final String REMOTE_BRANCH_DEMO_TEXT = new StringBand()
      .append("<remoteBranch>Remote_Branch</remoteBranch>")
      .append(" <status>1 // 2</status>")
      .append(" <headTags>1.0.0, 1.1.0</headTags>")
      .append(" <changedCount>5 changes</changedCount>")
      .toString();

  private static final String INLINE_BLAME_DEMO_TEXT = new StringBand()
      .append("var text = \"Some text\"")
      .append(" <editorInlineBlame>Blame: Kilroy 15.07.1410</editorInlineBlame>")
      .toString();

  private static final String MASTER_DEMO_TEXT = new StringBand()
      .append("<localMaster>master (local)</localMaster>")
      .append("\n")
      .append("<masterWithRemote>master (with remote)</masterWithRemote>")
      .append("\n")
      .toString();

  private static final String DEMO_TEXT = new StringBand()
      .append(LOCAL_BRANCH_DEMO_TEXT)
      .append("\n")
      .append(REMOTE_BRANCH_DEMO_TEXT)
      .append("\n")
      .append(MASTER_DEMO_TEXT)
      .append("\n")
      .append(INLINE_BLAME_DEMO_TEXT)
      .toString();

  private static final Map<String, TextAttributesKey> ADDITIONAL_MAPPINGS =
      ImmutableMap.<String, TextAttributesKey>builder()
      .put("remoteBranch", DecorationColors.REMOTE_BRANCH_ATTRIBUTES)
      .put("status", DecorationColors.STATUS_ATTRIBUTES)
      .put("headTags", DecorationColors.HEAD_TAGS_ATTRIBUTES)
      .put("localBranch", DecorationColors.LOCAL_BRANCH_ATTRIBUTES)
      .put("changedCount", DecorationColors.CHANGED_COUNT_ATTRIBUTES)
      .put("editorInlineBlame", DecorationColors.EDITOR_INLINE_BLAME_ATTRIBUTES)
      .put("masterWithRemote", DecorationColors.MASTER_WITH_REMOTE_ATTRIBUTES)
      .put("localMaster", DecorationColors.MASTER_LOCAL_ATTRIBUTES)
      .build();

  @NotNull
  @Override
  public AttributesDescriptor[] getAttributeDescriptors() {
    return new AttributesDescriptor[] {
      new AttributesDescriptor(ResBundle.message("colors.projectView.remote.branch.decoration.label"),
          DecorationColors.REMOTE_BRANCH_ATTRIBUTES),
      new AttributesDescriptor(ResBundle.message("colors.projectView.master.with.remote.decoration.label"),
          DecorationColors.MASTER_WITH_REMOTE_ATTRIBUTES),
      new AttributesDescriptor(ResBundle.message("colors.projectView.master.local.decoration.label"),
          DecorationColors.MASTER_LOCAL_ATTRIBUTES),
      new AttributesDescriptor(ResBundle.message("colors.projectView.status.decoration.label"),
            DecorationColors.STATUS_ATTRIBUTES),
      new AttributesDescriptor(ResBundle.message("colors.projectView.head.tags.decoration.label"),
            DecorationColors.HEAD_TAGS_ATTRIBUTES),
      new AttributesDescriptor(ResBundle.message("colors.projectView.local.branch.decoration.label"),
          DecorationColors.LOCAL_BRANCH_ATTRIBUTES),
      new AttributesDescriptor(ResBundle.message("colors.projectView.changes.decoration.label"),
          DecorationColors.CHANGED_COUNT_ATTRIBUTES),
      new AttributesDescriptor(ResBundle.message("colors.editor.inline.blame.label"),
          DecorationColors.EDITOR_INLINE_BLAME_ATTRIBUTES)
    };
  }

  @NotNull
  @Override
  public ColorDescriptor[] getColorDescriptors() {
    return new ColorDescriptor[0];
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return ResBundle.message("colors.page.display.label");
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return null;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return new PlainSyntaxHighlighter();
  }

  @NotNull
  @Override
  public String getDemoText() {
    return DEMO_TEXT;
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ADDITIONAL_MAPPINGS;
  }
}

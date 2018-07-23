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
  private static final String DEMO_TEXT = new StringBand("<localBranch>Local_Branch</localBranch>\n")
                                                  .append("<remoteBranch>Remote_Branch</remoteBranch>\n")
                                                  .append("<tags>1.0.0, 1.1.0</remoteBranch>")
                                                  .toString();
  private static final Map<String, TextAttributesKey> ADDITIONAL_MAPPINGS =
                                                  ImmutableMap.<String, TextAttributesKey>builder()
                                                  .put("localBranch", DecorationColors.LOCAL_BRANCH_ATTRIBUTES)
                                                  .put("remoteBranch", DecorationColors.REMOTE_BRANCH_ATTRIBUTES)
                                                  .put("tags", DecorationColors.TAGS_ATTRIBUTES)
                                                  .build();

  @NotNull
  @Override
  public AttributesDescriptor[] getAttributeDescriptors() {
    return new AttributesDescriptor[] {
      new AttributesDescriptor(ResBundle.getString("colors.projectView.remote.branch.decoration.label"),
          DecorationColors.REMOTE_BRANCH_ATTRIBUTES),
      new AttributesDescriptor(ResBundle.getString("colors.projectView.local.branch.decoration.label"),
          DecorationColors.LOCAL_BRANCH_ATTRIBUTES),
      new AttributesDescriptor(ResBundle.getString("colors.projectView.tags.decoration.label"),
          DecorationColors.TAGS_ATTRIBUTES)
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
    return ResBundle.getString("app.displayName");
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

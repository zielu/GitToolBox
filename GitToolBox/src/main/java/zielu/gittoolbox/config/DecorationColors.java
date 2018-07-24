package zielu.gittoolbox.config;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

public class DecorationColors {
  public static final TextAttributesKey REMOTE_BRANCH_ATTRIBUTES = TextAttributesKey
      .createTextAttributesKey("GIT_TOOLBOX.REMOTE_BRANCH_ATTRIBUTES");
  public static final TextAttributesKey LOCAL_BRANCH_ATTRIBUTES = TextAttributesKey
      .createTextAttributesKey("GIT_TOOLBOX.LOCAL_BRANCH_ATTRIBUTES");
  public static final TextAttributesKey HEAD_TAGS_ATTRIBUTES = TextAttributesKey
      .createTextAttributesKey("GIT_TOOLBOX.HEAD_TAGS_ATTRIBUTES");

  private DecorationColors() {
    throw new IllegalStateException();
  }

  public static SimpleTextAttributes simpleAttributes(@NotNull TextAttributesKey key) {
    EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
    return SimpleTextAttributes.fromTextAttributes(scheme.getAttributes(key));
  }
}

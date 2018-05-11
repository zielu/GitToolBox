package zielu.gittoolbox.config;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

public class DecorationColors {
  public static final TextAttributesKey LOCAL_BRANCH_ATTRIBUTES = TextAttributesKey
      .createTextAttributesKey("LOCAL_BRANCH_ATTRIBUTES");

  private DecorationColors() {
    throw new IllegalStateException();
  }

  public static SimpleTextAttributes simpleAttributes(@NotNull TextAttributesKey key) {
    EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
    return SimpleTextAttributes.fromTextAttributes(scheme.getAttributes(key));
  }
}

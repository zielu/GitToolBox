package zielu.gittoolbox.ui.projectview;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.DecorationColors;

public class DecorationColorsTextAttributesUi implements TextAttributesUi {
  private static final TextAttributesUi INSTANCE = new DecorationColorsTextAttributesUi();

  private DecorationColorsTextAttributesUi() {
  }

  public static TextAttributesUi getInstance() {
    return INSTANCE;
  }

  @Override
  public SimpleTextAttributes getTextAttributes(@NotNull TextAttributesKey key) {
    return DecorationColors.simpleAttributes(key);
  }
}

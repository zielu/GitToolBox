package zielu.gittoolbox.ui.projectview;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

public interface TextAttributesUi {
  SimpleTextAttributes getTextAttributes(@NotNull TextAttributesKey key);
}

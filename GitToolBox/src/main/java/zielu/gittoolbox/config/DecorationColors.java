package zielu.gittoolbox.config;

import com.intellij.openapi.editor.colors.TextAttributesKey;

public class DecorationColors {
  public static final TextAttributesKey LOCAL_BRANCH_ATTRIBUTES = TextAttributesKey
      .createTextAttributesKey("LOCAL_BRANCH_ATTRIBUTES");

  private DecorationColors() {
    throw new IllegalStateException();
  }
}

package zielu.intellij.ui;

import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

public class ZUiUtil {
  private ZUiUtil() {
    //do nothing
  }

  @NotNull
  public static String asHtml(@NotNull String content) {
    String contentWithConvertedNewLines = content.replace("\n", "<br>");
    return "<html><head>" + UIUtil.getCssFontDeclaration(UIUtil.getLabelFont()) + "</head><body>"
        + contentWithConvertedNewLines
        + "</body></html>";
  }
}

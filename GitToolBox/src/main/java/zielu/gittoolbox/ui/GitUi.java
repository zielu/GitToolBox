package zielu.gittoolbox.ui;

import com.intellij.util.ui.UIUtil;
import java.awt.Font;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.UtfSeq;

public enum GitUi {
    ;

    public static String upArrow() {
        Font font = UIUtil.getLabelFont();
        return canDisplay(font, UtfSeq.arrowUp, "\u02C4");
    }

    public static String downArrow() {
        Font font = UIUtil.getLabelFont();
        return canDisplay(font, UtfSeq.arrowDown, "\u02C5");
    }

    private static String canDisplay(@NotNull Font font, @NotNull String sequence, @NotNull String fallback) {
        if (font.canDisplayUpTo(sequence) == -1) {
            return sequence;
        } else {
            return fallback;
        }
    }
}

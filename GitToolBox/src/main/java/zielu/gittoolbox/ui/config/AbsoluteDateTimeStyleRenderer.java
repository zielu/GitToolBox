package zielu.gittoolbox.ui.config;

import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import java.util.Date;
import javax.swing.JList;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.config.AbsoluteDateTimeStyle;

public class AbsoluteDateTimeStyleRenderer extends ColoredListCellRenderer<AbsoluteDateTimeStyle> {
  private final Date now = new Date();

  @Override
  protected void customizeCellRenderer(@NotNull JList<? extends AbsoluteDateTimeStyle> list,
                                       AbsoluteDateTimeStyle value, int index, boolean selected, boolean hasFocus) {
    append(value.getLabel(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    append("    " + ResBundle.example() + "  " + value.getFormat().format(now),
        SimpleTextAttributes.GRAY_ATTRIBUTES);
  }
}

package zielu.gittoolbox.ui.config;

import com.intellij.ui.ColoredListCellRenderer;
import javax.swing.JList;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.ReferencePointForStatusType;

public class ReferencePointForStatusTypeRenderer extends ColoredListCellRenderer<ReferencePointForStatusType> {
  @Override
  protected void customizeCellRenderer(@NotNull JList<? extends ReferencePointForStatusType> list,
                                       ReferencePointForStatusType value, int index, boolean selected,
                                       boolean hasFocus) {
    if (value != null) {
      append(value.getLabel());
    }
  }
}

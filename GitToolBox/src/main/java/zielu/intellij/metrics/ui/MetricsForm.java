package zielu.intellij.metrics.ui;

import com.intellij.ui.table.JBTable;
import javax.swing.JComponent;
import javax.swing.JPanel;
import zielu.intellij.ui.GtFormUi;

public class MetricsForm implements GtFormUi {
  private JPanel content;
  private JBTable table;

  @Override
  public void init() {

  }

  @Override
  public JComponent getContent() {
    return content;
  }

  @Override
  public void afterStateSet() {

  }

  @Override
  public void dispose() {

  }
}

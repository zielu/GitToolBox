package zielu.intellij.ui;

import javax.swing.JComponent;

public interface GtFormUi {
  void init();

  JComponent getContent();

  void afterStateSet();

  void dispose();
}

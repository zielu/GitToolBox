package zielu.intellij.ui;

import com.intellij.openapi.Disposable;
import javax.swing.JComponent;

public interface GtFormUi extends Disposable {
  void init();

  JComponent getContent();

  void afterStateSet();

  default void dispose() {
    // do nothing
  }
}

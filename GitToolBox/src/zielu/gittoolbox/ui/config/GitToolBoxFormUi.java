package zielu.gittoolbox.ui.config;

import javax.swing.JComponent;

public interface GitToolBoxFormUi {
    void init();
    JComponent getContent();
    void afterStateSet();
}

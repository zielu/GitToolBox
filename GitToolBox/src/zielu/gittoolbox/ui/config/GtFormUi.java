package zielu.gittoolbox.ui.config;

import javax.swing.JComponent;

public interface GtFormUi {
    void init();
    JComponent getContent();
    void afterStateSet();
    void dispose();
}

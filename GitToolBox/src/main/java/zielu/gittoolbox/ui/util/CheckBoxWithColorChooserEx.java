package zielu.gittoolbox.ui.util;

import com.intellij.ui.CheckBoxWithColorChooser;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseListener;
import javax.swing.JButton;

public class CheckBoxWithColorChooserEx extends CheckBoxWithColorChooser {
    private MouseListener[] disabledListeners;

    public CheckBoxWithColorChooserEx(String text, boolean selected, Color color) {
        super(text, selected, color);
    }

    public CheckBoxWithColorChooserEx(String text, boolean selected) {
        super(text, selected);
    }

    public CheckBoxWithColorChooserEx(String text) {
        super(text);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        synchronized (getTreeLock()) {
            for (Component component : getComponents()) {
                component.setEnabled(enabled);
                if (component instanceof JButton) {
                    if (enabled) {
                        if (disabledListeners != null) {
                            for (MouseListener listener : disabledListeners) {
                                component.addMouseListener(listener);
                            }
                        }
                        disabledListeners = null;
                    } else {
                        disabledListeners = component.getMouseListeners();
                        for (MouseListener listener : disabledListeners) {
                            component.removeMouseListener(listener);
                        }
                    }
                }
            }
        }
    }

    public void dispose() {
        disabledListeners = null;
    }
}

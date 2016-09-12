package zielu.gittoolbox.ui.config;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import zielu.gittoolbox.fetch.AutoFetchParams;

public class GtPrjForm implements GtFormUi {
    private JPanel content;
    private JCheckBox autoFetchEnabledCheckBox;
    private JSpinner autoFetchIntervalSpinner;

    @Override
    public void init() {
        autoFetchIntervalSpinner.setModel(new SpinnerNumberModel(
            AutoFetchParams.defaultIntervalMinutes,
            AutoFetchParams.intervalMinMinutes,
            AutoFetchParams.intervalMaxMinutes,
            1
        ));
        autoFetchIntervalSpinner.setEnabled(false);
        autoFetchEnabledCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                autoFetchIntervalSpinner.setEnabled(autoFetchEnabledCheckBox.isSelected());
            }
        });
    }

    @Override
    public void afterStateSet() {}

    @Override
    public void dispose() {}

    public boolean getAutoFetchEnabled() {
        return autoFetchEnabledCheckBox.isSelected();
    }

    public void setAutoFetchEnabled(boolean autoFetchEnabled) {
        autoFetchEnabledCheckBox.setSelected(autoFetchEnabled);
    }

    public int getAutoFetchInterval() {
        return (Integer) autoFetchIntervalSpinner.getValue();
    }

    public void setAutoFetchInterval(int autoFetchInterval) {
        autoFetchIntervalSpinner.setValue(autoFetchInterval);
    }

    @Override
    public JComponent getContent() {
        return content;
    }
}

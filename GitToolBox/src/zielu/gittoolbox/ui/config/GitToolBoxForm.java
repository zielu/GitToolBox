package zielu.gittoolbox.ui.config;

import com.intellij.ui.ListCellRendererWrapper;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import zielu.gittoolbox.ui.StatusPresenter;
import zielu.gittoolbox.ui.StatusPresenters;

public class GitToolBoxForm {
    private JComboBox presentationMode;
    private JPanel content;
    private JCheckBox showGitStatCheckBox;
    private JCheckBox showProjectViewStatusCheckBox;
    private JCheckBox autoFetchEnabledCheckBox;
    private JSpinner autoFetchIntervalSpinner;

    public void init() {
        presentationMode.setRenderer(new ListCellRendererWrapper<StatusPresenter>() {
            @Override
            public void customize(JList jList, StatusPresenter presenter, int index,
                                  boolean isSelected, boolean hasFocus) {
                setText(presenter.getLabel());
            }
        });
        presentationMode.setModel(new DefaultComboBoxModel(StatusPresenters.values()));
        autoFetchIntervalSpinner.setModel(new SpinnerNumberModel(15, 10, 180, 1));
        autoFetchEnabledCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                autoFetchIntervalSpinner.setEnabled(autoFetchEnabledCheckBox.isEnabled());
            }
        });
    }

    public JComponent getContent() {
        return content;
    }

    public void setPresenter(StatusPresenter presenter) {
        presentationMode.setSelectedItem(presenter);
    }

    public StatusPresenter getPresenter() {
        return (StatusPresenter) presentationMode.getSelectedItem();
    }

    public void setShowGitStatus(boolean showGitStatus) {
        showGitStatCheckBox.setSelected(showGitStatus);
    }

    public boolean getShowGitStatus() {
        return showGitStatCheckBox.isSelected();
    }

    public void setShowProjectViewStatus(boolean showProjectViewStatus) {
        showProjectViewStatusCheckBox.setSelected(showProjectViewStatus);
    }

    public boolean getShowProjectViewStatus() {
        return showProjectViewStatusCheckBox.isSelected();
    }

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
}

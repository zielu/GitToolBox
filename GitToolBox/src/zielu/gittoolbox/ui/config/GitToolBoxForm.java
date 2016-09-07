package zielu.gittoolbox.ui.config;

import com.intellij.ui.ListCellRendererWrapper;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import zielu.gittoolbox.ui.StatusPresenter;
import zielu.gittoolbox.ui.StatusPresenters;

public class GitToolBoxForm implements GitToolBoxFormUi {
    private JComboBox presentationMode;
    private JPanel content;
    private JCheckBox showGitStatCheckBox;
    private JCheckBox showProjectViewStatusCheckBox;
    private JCheckBox behindTrackerEnabledCheckBox;
    private JCheckBox showLocationPathCheckBox;
    private JCheckBox showStatusBeforeLocationCheckBox;
    private JLabel presentationStatusBarPreview;
    private JLabel presentationProjectViewPreview;

    @Override
    public void init() {
        presentationMode.setRenderer(new ListCellRendererWrapper<StatusPresenter>() {
            @Override
            public void customize(JList jList, StatusPresenter presenter, int index,
                                  boolean isSelected, boolean hasFocus) {
                setText(presenter.getLabel());
            }
        });
        presentationMode.setModel(new DefaultComboBoxModel(StatusPresenters.values()));
        presentationMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StatusPresenter presenter = getPresenter();
                presentationStatusBarPreview.setText(getStatusBarPreview(presenter));
                presentationProjectViewPreview.setText(getProjectViewPreview(presenter));
            }
        });
        showProjectViewStatusCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                onProjectViewStatusChange();
            }
        });
        showLocationPathCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                onProjectViewStatusChange();
            }
        });
    }

    private String getStatusBarPreview(StatusPresenter presenter) {
        return presenter.aheadBehindStatus(3, 2)
            + " | " + presenter.aheadBehindStatus(3, 0)
            + " | " + presenter.aheadBehindStatus(0, 2);
    }

    private String getProjectViewPreview(StatusPresenter presenter) {
        return presenter.nonZeroAheadBehindStatus(3, 2)
            + " | " + presenter.nonZeroAheadBehindStatus(3, 0)
            + " | " + presenter.nonZeroAheadBehindStatus(0, 2);
    }

    private void onProjectViewStatusChange() {
        boolean enabled = showProjectViewStatusCheckBox.isSelected();
        showLocationPathCheckBox.setEnabled(enabled);
        if (enabled) {
            showStatusBeforeLocationCheckBox.setEnabled(showLocationPathCheckBox.isSelected());
        } else {
            showStatusBeforeLocationCheckBox.setEnabled(false);
        }
    }

    @Override
    public void afterStateSet() {
        onProjectViewStatusChange();
    }

    @Override
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

    public void setShowProjectViewLocationPath(boolean showProjectViewLocationPath) {
        showLocationPathCheckBox.setSelected(showProjectViewLocationPath);
    }

    public boolean getShowProjectViewLocationPath() {
        return showLocationPathCheckBox.isSelected();
    }

    public void setShowProjectViewStatusBeforeLocation(boolean showProjectViewStatusBeforeLocation) {
        showStatusBeforeLocationCheckBox.setSelected(showProjectViewStatusBeforeLocation);
    }

    public boolean getShowProjectViewStatusBeforeLocation() {
        return showStatusBeforeLocationCheckBox.isSelected();
    }

    public boolean getBehindTrackerEnabled() {
        return behindTrackerEnabledCheckBox.isSelected();
    }

    public void setBehindTrackerEnabled(boolean behindTrackerEnabled) {
        behindTrackerEnabledCheckBox.setSelected(behindTrackerEnabled);
    }
}

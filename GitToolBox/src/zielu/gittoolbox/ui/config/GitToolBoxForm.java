package zielu.gittoolbox.ui.config;

import com.intellij.ui.ListCellRendererWrapper;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import zielu.gittoolbox.ui.StatusPresenter;
import zielu.gittoolbox.ui.StatusPresenters;

public class GitToolBoxForm {
    private JComboBox presentationMode;
    private JPanel content;

    public void init() {
        presentationMode.setRenderer(new ListCellRendererWrapper<StatusPresenter>() {
            @Override
            public void customize(JList jList, StatusPresenter presenter, int index,
                                  boolean isSelected, boolean hasFocus) {
                setText(presenter.getLabel());
            }
        });
        presentationMode.setModel(new DefaultComboBoxModel(StatusPresenters.values()));
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
}

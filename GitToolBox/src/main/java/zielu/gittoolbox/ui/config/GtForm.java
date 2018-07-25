package zielu.gittoolbox.ui.config;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxUpdateProjectApp;
import zielu.gittoolbox.config.DecorationPartConfig;
import zielu.gittoolbox.config.DecorationPartType;
import zielu.gittoolbox.extension.UpdateProjectAction;
import zielu.gittoolbox.status.BehindStatus;
import zielu.gittoolbox.ui.StatusPresenter;
import zielu.gittoolbox.ui.StatusPresenters;
import zielu.intellij.ui.GtFormUi;

public class GtForm implements GtFormUi {
  private ComboBox presentationMode;
  private JPanel content;
  private JCheckBox showGitStatCheckBox;
  private JCheckBox showProjectViewStatusCheckBox;
  private JCheckBox behindTrackerEnabledCheckBox;
  private JCheckBox showLocationPathCheckBox;
  private JCheckBox showStatusBeforeLocationCheckBox;
  private JLabel presentationStatusBarPreview;
  private JLabel presentationProjectViewPreview;
  private JLabel presentationBehindTrackerPreview;
  private ComboBox updateProjectAction;
  private JCheckBox showTagsOnHeadCheckBox;
  private JPanel decorationLayoutPanel;
  private com.intellij.ui.components.JBTextField textField1;
  private com.intellij.ui.components.JBTextField textField2;
  private com.intellij.ui.components.JBTextField layoutPreviewTextField;

  @Override
  public void init() {
    JBPopupMenu addDecorationLayoutPartPopup = new JBPopupMenu();
    CollectionListModel<DecorationPartConfig> decorationPartsModel = new CollectionListModel<>(new ArrayList<>());

    Arrays.stream(DecorationPartType.values()).forEach(type -> {
      Action action = new AbstractActionExt() {
        {
          setName(type.getLabel());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
          DecorationPartConfig config = new DecorationPartConfig();
          config.type = type;
          decorationPartsModel.add(config);
        }
      };
      addDecorationLayoutPartPopup.add(action);
    });

    JBList<DecorationPartConfig> decorationLayoutList = new JBList<>(decorationPartsModel);
    decorationLayoutList.setCellRenderer(new ListCellRendererWrapper<DecorationPartConfig>() {
      @Override
      public void customize(JList list, DecorationPartConfig value, int index, boolean selected, boolean hasFocus) {
        setText(value.prefix + value.type.getPlaceholder() + value.postfix);
      }
    });
    ToolbarDecorator decorationToolbar = ToolbarDecorator.createDecorator(decorationLayoutList);
    decorationToolbar.setAddAction(button -> {
      RelativePoint popupPoint = button.getPreferredPopupPoint();
      Point point = popupPoint.getPoint();
      addDecorationLayoutPartPopup.show(popupPoint.getComponent(), point.x, point.y);
    });
    decorationLayoutPanel.add(decorationToolbar.createPanel(), BorderLayout.CENTER);

    presentationMode.setRenderer(new ListCellRendererWrapper<StatusPresenter>() {
      @Override
      public void customize(JList list, StatusPresenter presenter, int index, boolean isSelected,
                            boolean hasFocus) {
        setText(presenter.getLabel());
      }
    });
    presentationMode.setModel(new DefaultComboBoxModel<>(StatusPresenters.values()));
    presentationMode.addActionListener(e -> {
      StatusPresenter presenter = getPresenter();
      presentationStatusBarPreview.setText(getStatusBarPreview(presenter));
      presentationProjectViewPreview.setText(getProjectViewPreview(presenter));
      presentationBehindTrackerPreview.setText(getBehindTrackerPreview(presenter));
    });
    showProjectViewStatusCheckBox.addItemListener(e -> onProjectViewStatusChange());
    showLocationPathCheckBox.addItemListener(e -> onProjectViewStatusChange());
    updateProjectAction.setRenderer(new ListCellRendererWrapper<UpdateProjectAction>() {

      @Override
      public void customize(JList list, UpdateProjectAction action, int index, boolean selected,
                            boolean hasFocus) {
        setText(action.getName());
      }
    });
    updateProjectAction.setModel(getUpdateModeModel());
  }

  @NotNull
  private ComboBoxModel<UpdateProjectAction> getUpdateModeModel() {
    return new DefaultComboBoxModel<>(new Vector<>(GitToolBoxUpdateProjectApp.getInstance().getAll()));
  }

  @Override
  public void dispose() {
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

  private String getBehindTrackerPreview(StatusPresenter presenter) {
    return presenter.behindStatus(BehindStatus.create(3, 1))
        + " | " + presenter.behindStatus(BehindStatus.create(3, -1))
        + " | " + presenter.behindStatus(BehindStatus.create(3));
  }

  private void onProjectViewStatusChange() {
    boolean enabled = showProjectViewStatusCheckBox.isSelected();
    showLocationPathCheckBox.setEnabled(enabled);
    showTagsOnHeadCheckBox.setEnabled(enabled);
    if (enabled) {
      showStatusBeforeLocationCheckBox.setEnabled(showLocationPathCheckBox.isSelected());
    } else {
      showStatusBeforeLocationCheckBox.setEnabled(false);
    }
    updateProjectAction.setEnabled(updateProjectAction.getItemCount() > 1);
  }

  @Override
  public void afterStateSet() {
    onProjectViewStatusChange();
  }

  @Override
  public JComponent getContent() {
    return content;
  }

  public StatusPresenter getPresenter() {
    return (StatusPresenter) presentationMode.getSelectedItem();
  }

  public void setPresenter(StatusPresenter presenter) {
    presentationMode.setSelectedItem(presenter);
  }

  public boolean getShowGitStatus() {
    return showGitStatCheckBox.isSelected();
  }

  public void setShowGitStatus(boolean showGitStatus) {
    showGitStatCheckBox.setSelected(showGitStatus);
  }

  public boolean getBehindTrackerEnabled() {
    return behindTrackerEnabledCheckBox.isSelected();
  }

  public void setBehindTrackerEnabled(boolean behindTrackerEnabled) {
    behindTrackerEnabledCheckBox.setSelected(behindTrackerEnabled);
  }

  public boolean getShowProjectViewStatus() {
    return showProjectViewStatusCheckBox.isSelected();
  }

  public void setShowProjectViewStatus(boolean showProjectViewStatus) {
    showProjectViewStatusCheckBox.setSelected(showProjectViewStatus);
  }

  public boolean getShowProjectViewLocationPath() {
    return showLocationPathCheckBox.isSelected();
  }

  public void setShowProjectViewLocationPath(boolean showProjectViewLocationPath) {
    showLocationPathCheckBox.setSelected(showProjectViewLocationPath);
  }

  public boolean getShowProjectViewStatusBeforeLocation() {
    return showStatusBeforeLocationCheckBox.isSelected();
  }

  public void setShowProjectViewStatusBeforeLocation(boolean showProjectViewStatusBeforeLocation) {
    showStatusBeforeLocationCheckBox.setSelected(showProjectViewStatusBeforeLocation);
  }

  public boolean getShowProjectTagsOnHead() {
    return showTagsOnHeadCheckBox.isSelected();
  }

  public void setShowProjectViewTagsOnHead(boolean showTagsOnHead) {
    showTagsOnHeadCheckBox.setSelected(showTagsOnHead);
  }

  public UpdateProjectAction getUpdateProjectAction() {
    return (UpdateProjectAction) updateProjectAction.getSelectedItem();
  }

  public void setUpdateProjectAction(UpdateProjectAction action) {
    updateProjectAction.setSelectedItem(action);
  }
}

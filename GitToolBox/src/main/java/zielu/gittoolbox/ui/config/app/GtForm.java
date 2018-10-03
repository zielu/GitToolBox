package zielu.gittoolbox.ui.config.app;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListDataEvent;
import jodd.util.StringBand;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxUpdateProjectApp;
import zielu.gittoolbox.config.DecorationPartConfig;
import zielu.gittoolbox.config.DecorationPartType;
import zielu.gittoolbox.extension.UpdateProjectAction;
import zielu.gittoolbox.ui.StatusPresenter;
import zielu.gittoolbox.ui.StatusPresenters;
import zielu.gittoolbox.ui.util.ListDataAnyChangeAdapter;
import zielu.intellij.ui.GtFormUi;

public class GtForm implements GtFormUi {
  private final Map<DecorationPartType, Component> decorationPartActions = new LinkedHashMap<>();
  private final CollectionListModel<DecorationPartConfig> decorationPartsModel =
      new CollectionListModel<>(new ArrayList<>());
  private final JBList<DecorationPartConfig> decorationLayoutList = new JBList<>(decorationPartsModel);
  private final JBPopupMenu addDecorationLayoutPartPopup = new JBPopupMenu();

  private ComboBox<StatusPresenter> presentationMode;
  private JPanel content;
  private JCheckBox showGitStatCheckBox;
  private JCheckBox showProjectViewStatusCheckBox;
  private JCheckBox behindTrackerEnabledCheckBox;
  private JLabel presentationStatusBarPreview;
  private JLabel presentationProjectViewPreview;
  private JLabel presentationBehindTrackerPreview;
  private ComboBox<UpdateProjectAction> updateProjectAction;
  private JPanel decorationLayoutPanel;
  private JBTextField decorationPartPrefixTextField;
  private JBTextField decorationPartPostfixTextField;
  private JBTextField layoutPreviewTextField;
  private JCheckBox lensBlameEnabledCheckBox;

  @Override
  public void init() {
    Arrays.stream(DecorationPartType.values()).forEach(type -> {
      Action action = new AbstractActionExt(type.getLabel()) {
        @Override
        public void actionPerformed(ActionEvent e) {
          DecorationPartConfig config = new DecorationPartConfig();
          config.type = type;
          decorationPartsModel.add(config);
          addDecorationLayoutPartPopup.remove(decorationPartActions.get(type));
          int lastAddedIndex = decorationPartsModel.getSize() - 1;
          decorationLayoutList.getSelectionModel().setSelectionInterval(lastAddedIndex, lastAddedIndex);
        }
      };
      decorationPartActions.put(type, new JMenuItem(action));
    });
    decorationPartsModel.addListDataListener(new ListDataAnyChangeAdapter() {
      @Override
      public void changed(ListDataEvent e) {
        updateDecorationLayoutPreview();
      }
    });

    decorationLayoutList.setCellRenderer(new ListCellRendererWrapper<DecorationPartConfig>() {
      @Override
      public void customize(JList list, DecorationPartConfig value, int index, boolean selected, boolean hasFocus) {
        setText(value.prefix + value.type.getPlaceholder() + value.postfix);
      }
    });
    decorationLayoutList.getSelectionModel().addListSelectionListener(event -> {
      if (!event.getValueIsAdjusting()) {
        int[] selectedIndices = decorationLayoutList.getSelectedIndices();
        boolean editorsEnabled = selectedIndices.length == 1;
        decorationPartPrefixTextField.setEnabled(editorsEnabled);
        decorationPartPostfixTextField.setEnabled(editorsEnabled);
        getCurrentDecorationPart().ifPresent(current -> {
          decorationPartPrefixTextField.setText(current.prefix);
          decorationPartPostfixTextField.setText(current.postfix);
        });
      }
    });
    ToolbarDecorator decorationToolbar = ToolbarDecorator.createDecorator(decorationLayoutList);
    decorationToolbar.setAddAction(button -> {
      RelativePoint popupPoint = button.getPreferredPopupPoint();
      Point point = popupPoint.getPoint();
      addDecorationLayoutPartPopup.show(popupPoint.getComponent(), point.x, point.y);
    });
    decorationToolbar.setRemoveAction(button -> {
      List<DecorationPartConfig> selected = decorationLayoutList.getSelectedValuesList();
      selected.forEach(config -> {
        addDecorationLayoutPartPopup.add(decorationPartActions.get(config.type));
        decorationPartsModel.remove(config);
      });
      updateDecorationLayoutPreview();
    });
    decorationLayoutPanel.add(decorationToolbar.createPanel(), BorderLayout.CENTER);
    decorationPartPrefixTextField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        updateCurrentDecorationPartPrefix(decorationPartPrefixTextField);
        updateDecorationLayoutPreview();
      }
    });
    decorationPartPostfixTextField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        updateCurrentDecorationPartPostfix(decorationPartPostfixTextField);
        updateDecorationLayoutPreview();
      }
    });

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
      presentationStatusBarPreview.setText(PresenterPreview.getStatusBarPreview(presenter));
      presentationProjectViewPreview.setText(PresenterPreview.getProjectViewPreview(presenter));
      presentationBehindTrackerPreview.setText(PresenterPreview.getBehindTrackerPreview(presenter));
    });
    showProjectViewStatusCheckBox.addItemListener(e -> onProjectViewStatusChange());
    updateProjectAction.setRenderer(new ListCellRendererWrapper<UpdateProjectAction>() {
      @Override
      public void customize(JList list, UpdateProjectAction action, int index, boolean selected,
                            boolean hasFocus) {
        setText(action.getName());
      }
    });
    updateProjectAction.setModel(getUpdateModeModel());
  }

  private Optional<DecorationPartConfig> getCurrentDecorationPart() {
    int[] selectedIndices = decorationLayoutList.getSelectedIndices();
    if (selectedIndices.length > 0) {
      return Optional.of(decorationPartsModel.getElementAt(selectedIndices[0]));
    } else {
      return Optional.empty();
    }
  }

  private void updateCurrentDecorationPartPrefix(JBTextField textField) {
    getCurrentDecorationPart().ifPresent(current -> {
      current.prefix = textField.getText();
      repaintDecorationPart();
    });
  }

  private void updateCurrentDecorationPartPostfix(JBTextField textField) {
    getCurrentDecorationPart().ifPresent(current -> {
      current.postfix = textField.getText();
      repaintDecorationPart();
    });
  }

  private void repaintDecorationPart() {
    decorationLayoutList.repaint();
  }

  private void updateDecorationLayoutPreview() {
    String preview = decorationPartsModel.getItems().stream().map(this::getDecorationPartPreview)
        .collect(Collectors.joining(" "));
    layoutPreviewTextField.setText(preview);
  }

  private String getDecorationPartPreview(DecorationPartConfig config) {
    StringBand preview = new StringBand(config.prefix);
    DecorationPartPreview
        .appendPreview(getPresenter(), config.type, preview)
        .append(config.postfix);
    return preview.toString();
  }

  @NotNull
  private ComboBoxModel<UpdateProjectAction> getUpdateModeModel() {
    return new DefaultComboBoxModel<>(new Vector<>(GitToolBoxUpdateProjectApp.getInstance().getAll()));
  }

  @Override
  public void dispose() {
  }

  private void onProjectViewStatusChange() {
    updateProjectAction.setEnabled(updateProjectAction.getItemCount() > 1);
  }

  @Override
  public void afterStateSet() {
    onProjectViewStatusChange();
    Arrays.stream(DecorationPartType.values()).filter(type -> !hasDecorationPart(type)).forEach(type -> {
      addDecorationLayoutPartPopup.add(decorationPartActions.get(type));
    });
    updateDecorationLayoutPreview();
  }

  private boolean hasDecorationPart(DecorationPartType type) {
    return decorationPartsModel.getItems().stream().anyMatch(config -> type == config.type);
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

  public void setShowLensBlame(boolean showLensBlame) {
    lensBlameEnabledCheckBox.setSelected(showLensBlame);
  }

  public boolean getShowLensBlame() {
    return lensBlameEnabledCheckBox.isSelected();
  }

  public UpdateProjectAction getUpdateProjectAction() {
    return (UpdateProjectAction) updateProjectAction.getSelectedItem();
  }

  public void setUpdateProjectAction(UpdateProjectAction action) {
    updateProjectAction.setSelectedItem(action);
  }

  public void setDecorationParts(List<DecorationPartConfig> decorationParts) {
    decorationPartsModel.removeAll();
    decorationParts.stream().map(DecorationPartConfig::copy).forEach(decorationPartsModel::add);
  }

  public List<DecorationPartConfig> getDecorationParts() {
    return decorationPartsModel.toList();
  }
}

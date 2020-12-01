package zielu.gittoolbox.ui.config.app;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListDataEvent;
import jodd.util.StringBand;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.AbsoluteDateTimeStyle;
import zielu.gittoolbox.config.AuthorNameType;
import zielu.gittoolbox.config.CommitCompletionMode;
import zielu.gittoolbox.config.DateType;
import zielu.gittoolbox.config.DecorationPartConfig;
import zielu.gittoolbox.config.DecorationPartType;
import zielu.gittoolbox.extension.update.UpdateProjectAction;
import zielu.gittoolbox.ui.StatusPresenter;
import zielu.gittoolbox.ui.StatusPresenters;
import zielu.gittoolbox.ui.config.AbsoluteDateTimeStyleRenderer;
import zielu.gittoolbox.ui.config.AppliedProjectsDialog;
import zielu.gittoolbox.ui.update.UpdateProjectActionService;
import zielu.gittoolbox.ui.util.ListDataAnyChangeAdapter;
import zielu.intellij.ui.GtFormUi;
import zielu.intellij.ui.ZUiProperties;
import zielu.intellij.util.ZProperty;

public class GtForm implements GtFormUi {
  private final Map<DecorationPartType, Component> decorationPartActions = new LinkedHashMap<>();
  private List<String> appliedAutoFetchEnabledPaths = Collections.emptyList();
  private List<String> appliedAutoFetchOnBranchSwitchEnabledPaths = Collections.emptyList();
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
  private JCheckBox statusBlameEnabledCheckBox;
  private JCheckBox editorInlineBlameEnabledCheckBox;
  private ComboBox<CommitCompletionMode> commitDialogCompletionMode;
  private ComboBox<AuthorNameType> blameInlineAuthorNameTypeCombo;
  private ComboBox<DateType> blameDateTypeCombo;
  private JCheckBox blameShowSubjectCheckBox;
  private ComboBox<AuthorNameType> blameStatusAuthorNameTypeCombo;
  private ComboBox<AbsoluteDateTimeStyle> absoluteDateTimeStyleCombo;
  private JCheckBox showChangesInStatusBarCheckBox;
  private JCheckBox autoFetchEnabledOverride;
  private JCheckBox autoFetchEnabled;
  private JButton appliedAutoFetchEnabled;
  private JCheckBox autoFetchOnBranchSwitchOverride;
  private JCheckBox autoFetchOnBranchSwitch;
  private JButton appliedAutoFetchOnBranchSwitchEnabled;
  private JCheckBox commitDialogGitmojiCompletionCheckBox;
  private JCheckBox alwaysShowInlineBlameWhileDebuggingCheckBox;
  private JCheckBox commitDialogGitmojiCompletionUnicodeCheckBox;

  private ZProperty<Boolean> alwaysShowInlineBlameWhileDebugging;

  @Override
  public void init() {
    DecorationPartType.getValues().stream().forEach(type -> {
      Action action = new AbstractActionExt(type.getLabel()) {
        @Override
        public void actionPerformed(ActionEvent e) {
          DecorationPartConfig config = new DecorationPartConfig(type);
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

    decorationLayoutList.setCellRenderer(new SimpleListCellRenderer<DecorationPartConfig>() {
      @Override
      public void customize(JList list, DecorationPartConfig value, int index, boolean selected, boolean hasFocus) {
        setText(value.getPrefix() + value.getType().getPlaceholder() + value.getPostfix());
      }
    });
    decorationLayoutList.getSelectionModel().addListSelectionListener(event -> {
      if (!event.getValueIsAdjusting()) {
        int[] selectedIndices = decorationLayoutList.getSelectedIndices();
        boolean editorsEnabled = selectedIndices.length == 1;
        decorationPartPrefixTextField.setEnabled(editorsEnabled);
        decorationPartPostfixTextField.setEnabled(editorsEnabled);
        getCurrentDecorationPart().ifPresent(current -> {
          decorationPartPrefixTextField.setText(current.getPrefix());
          decorationPartPostfixTextField.setText(current.getPostfix());
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
        addDecorationLayoutPartPopup.add(decorationPartActions.get(config.getType()));
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

    presentationMode.setRenderer(new SimpleListCellRenderer<StatusPresenter>() {
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
    updateProjectAction.setRenderer(new SimpleListCellRenderer<UpdateProjectAction>() {
      @Override
      public void customize(JList list, UpdateProjectAction action, int index, boolean selected,
                            boolean hasFocus) {
        setText(action.getName());
      }
    });
    updateProjectAction.setModel(getUpdateModeModel());
    commitDialogCompletionMode.setRenderer(new SimpleListCellRenderer<CommitCompletionMode>() {
      @Override
      public void customize(JList list, CommitCompletionMode value, int index, boolean selected, boolean hasFocus) {
        setText(value.getDisplayLabel());
      }
    });
    commitDialogCompletionMode.setModel(new DefaultComboBoxModel<>(CommitCompletionMode.values()));
    blameInlineAuthorNameTypeCombo.setRenderer(createAuthorNameTypeRenderer());
    blameInlineAuthorNameTypeCombo.setModel(new CollectionComboBoxModel<>(AuthorNameType.getInlineBlame()));
    blameStatusAuthorNameTypeCombo.setRenderer(createAuthorNameTypeRenderer());
    blameStatusAuthorNameTypeCombo.setModel(new CollectionComboBoxModel<>(AuthorNameType.getStatusBlame()));
    blameDateTypeCombo.setRenderer(new SimpleListCellRenderer<DateType>() {
      @Override
      public void customize(JList list, DateType value, int index, boolean selected, boolean hasFocus) {
        setText(value.getDisplayLabel());
      }
    });
    blameDateTypeCombo.setModel(new DefaultComboBoxModel<>(DateType.values()));
    absoluteDateTimeStyleCombo.setRenderer(new AbsoluteDateTimeStyleRenderer());
    absoluteDateTimeStyleCombo.setModel(new DefaultComboBoxModel<>(AbsoluteDateTimeStyle.values()));

    autoFetchEnabledOverride.addItemListener(e -> onAutoFetchEnabledOverride());
    appliedAutoFetchEnabled.addActionListener(e -> showAppliedAutoFetchEnabled());
    autoFetchOnBranchSwitchOverride.addItemListener(e -> onAutoFetchOnBranchSwitchEnabledOverride());
    appliedAutoFetchOnBranchSwitchEnabled.addActionListener(e -> showAppliedAutoFetchEnabledOnBranchSwitch());

    alwaysShowInlineBlameWhileDebugging = ZUiProperties.createSelectedProperty(
        alwaysShowInlineBlameWhileDebuggingCheckBox);
  }

  private ListCellRenderer<AuthorNameType> createAuthorNameTypeRenderer() {
    return new SimpleListCellRenderer<AuthorNameType>() {
      @Override
      public void customize(JList list, AuthorNameType value, int index, boolean selected, boolean hasFocus) {
        setText(value.getDisplayLabel());
      }
    };
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
    updateCurrentDecorationPart(current -> current.copy(current.getType(), textField.getText(), current.getPostfix()));
  }

  private void updateCurrentDecorationPartPostfix(JBTextField textField) {
    updateCurrentDecorationPart(current -> current.copy(current.getType(), current.getPrefix(), textField.getText()));
  }

  private void updateCurrentDecorationPart(Function<DecorationPartConfig, DecorationPartConfig> update) {
    int[] selectedIndices = decorationLayoutList.getSelectedIndices();
    if (selectedIndices.length > 0) {
      int index = selectedIndices[0];
      DecorationPartConfig current = decorationPartsModel.getElementAt(index);
      DecorationPartConfig updated = update.apply(current);
      decorationPartsModel.setElementAt(updated, index);
      repaintDecorationPart();
    }
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
    StringBand preview = new StringBand(config.getPrefix());
    DecorationPartPreview
        .appendPreview(getPresenter(), config.getType(), preview)
        .append(config.getPostfix());
    return preview.toString();
  }

  @NotNull
  private ComboBoxModel<UpdateProjectAction> getUpdateModeModel() {
    return new DefaultComboBoxModel<>(new Vector<>(UpdateProjectActionService.getInstance().getAll()));
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
    DecorationPartType.getValues().stream().filter(type -> !hasDecorationPart(type)).forEach(type -> {
      addDecorationLayoutPartPopup.add(decorationPartActions.get(type));
    });
    updateDecorationLayoutPreview();
    onAutoFetchEnabledOverride();
    onAutoFetchOnBranchSwitchEnabledOverride();
  }

  private boolean hasDecorationPart(DecorationPartType type) {
    return decorationPartsModel.getItems().stream().anyMatch(config -> type == config.getType());
  }

  private void onAutoFetchEnabledOverride() {
    autoFetchEnabled.setEnabled(autoFetchEnabledOverride.isSelected());
  }

  private void onAutoFetchOnBranchSwitchEnabledOverride() {
    autoFetchOnBranchSwitch.setEnabled(autoFetchOnBranchSwitchOverride.isSelected());
  }

  private void showAppliedAutoFetchEnabled() {
    showApplied(appliedAutoFetchEnabledPaths);
  }

  private void showAppliedAutoFetchEnabledOnBranchSwitch() {
    showApplied(appliedAutoFetchOnBranchSwitchEnabledPaths);
  }

  private void showApplied(List<String> paths) {
    AppliedProjectsDialog dialog = new AppliedProjectsDialog(content);
    dialog.setAppliedPaths(paths);
    dialog.show();
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

  boolean getShowGitStatus() {
    return showGitStatCheckBox.isSelected();
  }

  void setShowGitStatus(boolean showGitStatus) {
    showGitStatCheckBox.setSelected(showGitStatus);
  }

  boolean getBehindTrackerEnabled() {
    return behindTrackerEnabledCheckBox.isSelected();
  }

  void setBehindTrackerEnabled(boolean behindTrackerEnabled) {
    behindTrackerEnabledCheckBox.setSelected(behindTrackerEnabled);
  }

  boolean getShowProjectViewStatus() {
    return showProjectViewStatusCheckBox.isSelected();
  }

  void setShowProjectViewStatus(boolean showProjectViewStatus) {
    showProjectViewStatusCheckBox.setSelected(showProjectViewStatus);
  }

  void setShowStatusBlame(boolean showBlame) {
    statusBlameEnabledCheckBox.setSelected(showBlame);
  }

  boolean getShowStatusBlame() {
    return statusBlameEnabledCheckBox.isSelected();
  }

  void setShowEditorInlineBlame(boolean showEditorInlineBlame) {
    editorInlineBlameEnabledCheckBox.setSelected(showEditorInlineBlame);
  }

  boolean getShowEditorInlineBlame() {
    return editorInlineBlameEnabledCheckBox.isSelected();
  }

  UpdateProjectAction getUpdateProjectAction() {
    return (UpdateProjectAction) updateProjectAction.getSelectedItem();
  }

  void setUpdateProjectAction(UpdateProjectAction action) {
    updateProjectAction.setSelectedItem(action);
  }

  void setDecorationParts(List<DecorationPartConfig> decorationParts) {
    decorationPartsModel.removeAll();
    decorationParts.stream().map(DecorationPartConfig::copy).forEach(decorationPartsModel::add);
  }

  List<DecorationPartConfig> getDecorationParts() {
    return decorationPartsModel.toList();
  }

  CommitCompletionMode getCommitDialogCompletionMode() {
    return (CommitCompletionMode) commitDialogCompletionMode.getSelectedItem();
  }

  void setCommitDialogCompletionMode(CommitCompletionMode mode) {
    commitDialogCompletionMode.setSelectedItem(mode);
  }

  AuthorNameType getBlameInlineAuthorNameType() {
    return (AuthorNameType) blameInlineAuthorNameTypeCombo.getSelectedItem();
  }

  void setBlameInlineAuthorNameType(AuthorNameType authorNameType) {
    blameInlineAuthorNameTypeCombo.setSelectedItem(authorNameType);
  }

  DateType getBlameDateType() {
    return (DateType) blameDateTypeCombo.getSelectedItem();
  }

  void setBlameDateType(DateType dateType) {
    blameDateTypeCombo.setSelectedItem(dateType);
  }

  boolean getBlameShowSubject() {
    return blameShowSubjectCheckBox.isSelected();
  }

  void setBlameShowSubject(boolean showSubject) {
    blameShowSubjectCheckBox.setSelected(showSubject);
  }

  AuthorNameType getBlameStatusAuthorNameType() {
    return (AuthorNameType) blameStatusAuthorNameTypeCombo.getSelectedItem();
  }

  void setBlameStatusAuthorNameType(AuthorNameType authorNameType) {
    blameStatusAuthorNameTypeCombo.setSelectedItem(authorNameType);
  }

  AbsoluteDateTimeStyle getAbsoluteDateTimeStyle() {
    return (AbsoluteDateTimeStyle) absoluteDateTimeStyleCombo.getSelectedItem();
  }

  void setAbsoluteDateTimeStyle(AbsoluteDateTimeStyle absoluteDateTimeStyle) {
    absoluteDateTimeStyleCombo.setSelectedItem(absoluteDateTimeStyle);
  }

  boolean getShowChangesInStatusBar() {
    return showChangesInStatusBarCheckBox.isSelected();
  }

  void setShowChangesInStatusBar(boolean showChangesInStatusBar) {
    showChangesInStatusBarCheckBox.setSelected(showChangesInStatusBar);
  }

  boolean getAutoFetchEnabledOverride() {
    return autoFetchEnabledOverride.isSelected();
  }

  void setAutoFetchEnabledOverride(boolean enabledOverride) {
    autoFetchEnabledOverride.setSelected(enabledOverride);
  }

  boolean getAutoFetchEnabled() {
    return autoFetchEnabled.isSelected();
  }

  void setAutoFetchEnabled(boolean enabled) {
    autoFetchEnabled.setSelected(enabled);
  }

  boolean getAutoFetchOnBranchSwitchEnabledOverride() {
    return autoFetchOnBranchSwitchOverride.isSelected();
  }

  void setAutoFetchOnBranchSwitchEnabledOverride(boolean enabledOverride) {
    autoFetchOnBranchSwitchOverride.setSelected(enabledOverride);
  }

  boolean getAutoFetchOnBranchSwitchEnabled() {
    return autoFetchOnBranchSwitch.isSelected();
  }

  void setAutoFetchOnBranchSwitchEnabled(boolean enabled) {
    autoFetchOnBranchSwitch.setSelected(enabled);
  }

  public void setAppliedAutoFetchEnabledPaths(List<String> appliedAutoFetchEnabledPaths) {
    this.appliedAutoFetchEnabledPaths = new ArrayList<>(appliedAutoFetchEnabledPaths);
  }

  public void setAppliedAutoFetchOnBranchSwitchEnabledPaths(List<String> appliedAutoFetchOnBranchSwitchEnabledPaths) {
    this.appliedAutoFetchOnBranchSwitchEnabledPaths = new ArrayList<>(appliedAutoFetchOnBranchSwitchEnabledPaths);
  }

  public void setCommitDialogGitmojiCompletionEnabled(boolean enabled) {
    commitDialogGitmojiCompletionCheckBox.setSelected(enabled);
  }

  public boolean getCommitDialogGitmojiCompletionEnabled() {
    return commitDialogGitmojiCompletionCheckBox.isSelected();
  }

  public void setCommitDialogGitmojiCompletionUnicode(boolean enabled) {
    commitDialogGitmojiCompletionUnicodeCheckBox.setSelected(enabled);
  }

  public boolean getCommitDialogGitmojiCompletionUnicode() {
    return commitDialogGitmojiCompletionUnicodeCheckBox.isSelected();
  }

  public ZProperty<Boolean> alwaysShowInlineBlameWhileDebugging() {
    return alwaysShowInlineBlameWhileDebugging;
  }
}

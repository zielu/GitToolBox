package zielu.gittoolbox.ui.config;

import com.intellij.icons.AllIcons.General;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBList;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.ResIcons;
import zielu.gittoolbox.config.CommitCompletionConfig;
import zielu.gittoolbox.config.CommitCompletionType;
import zielu.gittoolbox.fetch.AutoFetchParams;
import zielu.gittoolbox.ui.util.AppUtil;
import zielu.gittoolbox.ui.util.ButtonWithPopup;
import zielu.gittoolbox.ui.util.SimpleAction;

public class GtPrjForm implements GtFormUi {
  private JPanel content;
  private JCheckBox autoFetchEnabledCheckBox;
  private JSpinner autoFetchIntervalSpinner;
  private JCheckBox commitCompletionCheckBox;
  private JPanel completionItemConfigPanel;
  private JBList<CommitCompletionConfig> completionItemList;
  private ButtonWithPopup completionItemAdd;
  private JButton completionItemRemove;

  private CollectionListModel<CommitCompletionConfig> completionItemModel = new CollectionListModel<>();
  private GtPatternFormatterForm completionItemPatternForm;

  private Action addSimpleCompletionAction;

  private void createUIComponents() {
    addSimpleCompletionAction = new AbstractActionExt() {
      {
        setName(ResBundle.getString("commit.dialog.completion.formatters.simple.add.label"));
        setSmallIcon(ResIcons.BranchOrange);
      }

      @Override
      public void actionPerformed(ActionEvent e) {
        completionItemModel.add(CommitCompletionConfig.create(CommitCompletionType.SIMPLE));
        updateCompletionItemActions();
      }
    };
    Action addPatternCompletionAction = new AbstractActionExt() {
      {
        setName(ResBundle.getString("commit.dialog.completion.formatters.pattern.add.label"));
        setSmallIcon(ResIcons.BranchViolet);
      }

      @Override
      public void actionPerformed(ActionEvent e) {
        completionItemModel.add(CommitCompletionConfig.create(CommitCompletionType.PATTERN));
        updateCompletionItemActions();
      }
    };

    SimpleAction addCompletionAction = new SimpleAction(General.Add);
    addCompletionAction.setShortDescription(ResBundle.getString("commit.dialog.completion.formatters.add.tooltip"));
    completionItemAdd = new ButtonWithPopup(addCompletionAction, addSimpleCompletionAction,
        addPatternCompletionAction);
  }

  @Override
  public void init() {
    completionItemPatternForm = new GtPatternFormatterForm();
    completionItemPatternForm.init();

    completionItemConfigPanel.add(completionItemPatternForm.getContent(), BorderLayout.CENTER);
    completionItemPatternForm.getContent().setVisible(false);

    autoFetchIntervalSpinner.setModel(new SpinnerNumberModel(
        AutoFetchParams.defaultIntervalMinutes,
        AutoFetchParams.intervalMinMinutes,
        AutoFetchParams.intervalMaxMinutes,
        1
    ));
    autoFetchIntervalSpinner.setEnabled(false);
    autoFetchEnabledCheckBox
        .addItemListener(e -> autoFetchIntervalSpinner.setEnabled(autoFetchEnabledCheckBox.isSelected()));
    completionItemList.setModel(completionItemModel);
    completionItemList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        ListSelectionModel selectionModel = completionItemList.getSelectionModel();
        if (selectionModel.isSelectionEmpty()) {
          onCompletionItemSelected(null);
        } else {
          CommitCompletionConfig selected = completionItemModel.getElementAt(selectionModel.getMinSelectionIndex());
          onCompletionItemSelected(selected);
        }
      }
    });
    completionItemList.setCellRenderer(new CommitCompletionConfigCellRenderer());
    completionItemRemove.setIcon(General.Remove);
    completionItemRemove.addActionListener(e -> {
      int selectedIndex = completionItemList.getSelectionModel().getMinSelectionIndex();
      if (selectedIndex > -1) {
        completionItemModel.removeRow(selectedIndex);
        updateCompletionItemActions();
      }
    });

    completionItemPatternForm.addPatternUpdate(text -> AppUtil.invokeLaterIfNeeded(() -> completionItemList.repaint()));
  }

  private void onCompletionItemSelected(CommitCompletionConfig config) {
    if (config == null) {
      completionItemPatternForm.getContent().setVisible(false);
    } else if (config.type == CommitCompletionType.PATTERN) {
      completionItemPatternForm.setCommitCompletionConfig(config);
      completionItemPatternForm.afterStateSet();
      completionItemPatternForm.getContent().setVisible(true);
    } else {
      completionItemPatternForm.getContent().setVisible(false);
    }
  }

  private void updateCompletionItemActions() {
    addSimpleCompletionAction.setEnabled(!completionItemModel.contains(getSimpleCompletion()));
  }

  private CommitCompletionConfig getSimpleCompletion() {
    return CommitCompletionConfig.create(CommitCompletionType.SIMPLE);
  }

  @Override
  public void afterStateSet() {
    updateCompletionItemActions();
  }

  @Override
  public void dispose() {
    completionItemPatternForm.dispose();
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

  public boolean getCommitCompletionEnabled() {
    return commitCompletionCheckBox.isSelected();
  }

  public void setCommitCompletionEnabled(boolean commitCompletionEnabled) {
    commitCompletionCheckBox.setSelected(commitCompletionEnabled);
  }

  public List<CommitCompletionConfig> getCommitCompletionConfigs() {
    return new ArrayList<>(completionItemModel.getItems());
  }

  public void setCommitCompletionConfigs(List<CommitCompletionConfig> configs) {
    completionItemModel.replaceAll(configs.stream().map(CommitCompletionConfig::copy).collect(Collectors.toList()));
  }

  @Override
  public JComponent getContent() {
    return content;
  }

  private static class CommitCompletionConfigCellRenderer extends ColoredListCellRenderer<CommitCompletionConfig> {
    private final EnumMap<CommitCompletionType, Icon> completionIcons = new EnumMap<>(CommitCompletionType.class);

    private CommitCompletionConfigCellRenderer() {
      completionIcons.put(CommitCompletionType.SIMPLE, ResIcons.BranchOrange);
      completionIcons.put(CommitCompletionType.PATTERN, ResIcons.BranchViolet);
    }

    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends CommitCompletionConfig> list,
                                         CommitCompletionConfig value, int index, boolean selected, boolean hasFocus) {
      setIcon(getIconForCompletion(value));
      append(value.getPresentableText());
    }

    private Icon getIconForCompletion(CommitCompletionConfig config) {
      return completionIcons.get(config.type);
    }
  }
}

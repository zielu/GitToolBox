package zielu.gittoolbox.ui.config;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.Icon;
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
import zielu.gittoolbox.util.GtUtil;
import zielu.intellij.ui.GtFormUi;

public class GtPrjForm implements GtFormUi {
  private final Logger log = Logger.getInstance(getClass());
  private final CollectionListModel<String> autoFetchExclusionsModel = new CollectionListModel<>();
  private final JBList<String> autoFetchExclusionsList = new JBList<>(autoFetchExclusionsModel);
  private final CollectionListModel<CommitCompletionConfig> completionItemModel = new CollectionListModel<>();
  private final JBList<CommitCompletionConfig> completionItemList = new JBList<>(completionItemModel);
  private final JBPopupMenu addCommitCompletionPopup = new JBPopupMenu();

  private JPanel content;
  private JCheckBox autoFetchEnabledCheckBox;
  private JSpinner autoFetchIntervalSpinner;
  private JCheckBox commitCompletionCheckBox;
  private JPanel completionItemConfigPanel;
  private JPanel commitCompletionPanel;

  private JPanel autoFetchExclusionsPanel;

  private GtPatternFormatterForm completionItemPatternForm;
  private Action addSimpleCompletionAction;
  private ToolbarDecorator autoFetchExclusionsDecorator;
  private Project project;

  @Override
  public void init() {
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
    addCommitCompletionPopup.add(addSimpleCompletionAction);
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
    addCommitCompletionPopup.add(addPatternCompletionAction);

    completionItemPatternForm = new GtPatternFormatterForm();
    completionItemPatternForm.init();

    completionItemConfigPanel.add(completionItemPatternForm.getContent(), BorderLayout.CENTER);
    completionItemPatternForm.getContent().setVisible(false);

    autoFetchIntervalSpinner.setModel(new SpinnerNumberModel(
        AutoFetchParams.DEFAULT_INTERVAL_MINUTES,
        AutoFetchParams.INTERVAL_MIN_MINUTES,
        AutoFetchParams.INTERVAL_MAX_MINUTES,
        1
    ));
    autoFetchIntervalSpinner.setEnabled(false);
    autoFetchEnabledCheckBox.addItemListener(e ->
        autoFetchIntervalSpinner.setEnabled(autoFetchEnabledCheckBox.isSelected()));
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
    completionItemPatternForm.addPatternUpdate(text -> AppUtil.INSTANCE
        .invokeLaterIfNeeded(completionItemList::repaint));
    ToolbarDecorator commitCompletionDecorator = ToolbarDecorator.createDecorator(completionItemList);
    commitCompletionDecorator.setAddAction(button -> {
      RelativePoint popupPoint = button.getPreferredPopupPoint();
      Point point = popupPoint.getPoint();
      addCommitCompletionPopup.show(popupPoint.getComponent(), point.x, point.y);
    });
    commitCompletionDecorator.setAddActionName(ResBundle.getString("commit.dialog.completion.formatters.add.tooltip"));
    commitCompletionDecorator.setRemoveAction(button -> onCommitCompletionItemRemove());
    commitCompletionDecorator.setRemoveActionName("commit.dialog.completion.formatters.remove.tooltip");
    commitCompletionPanel.add(commitCompletionDecorator.createPanel(), BorderLayout.CENTER);

    autoFetchExclusionsDecorator = ToolbarDecorator.createDecorator(autoFetchExclusionsList);
    autoFetchExclusionsDecorator.setAddAction(button -> onAddAutoFetchExclusion());
    autoFetchExclusionsDecorator.setAddActionName(
        ResBundle.getString("configurable.prj.autoFetch.exclusions.add.label"));
    autoFetchExclusionsDecorator.setRemoveAction(button -> onRemoveAutoFetchExclusion());
    autoFetchExclusionsDecorator.setRemoveActionName(
        ResBundle.getString("configurable.prj.autoFetch.exclusions.remove.label"));
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

  private void onCommitCompletionItemRemove() {
    int selectedIndex = completionItemList.getSelectionModel().getMinSelectionIndex();
    if (selectedIndex > -1) {
      completionItemModel.removeRow(selectedIndex);
      updateCompletionItemActions();
    }
  }

  private void onAddAutoFetchExclusion() {
    log.debug("Add exclusions...");
    GtRepoChooser chooser = new GtRepoChooser(project, content);
    List<GitRepository> excluded = GtUtil.getRepositoriesForRoots(project, autoFetchExclusionsModel.getItems());
    log.debug("Currently excluded: ", excluded);
    chooser.setSelectedRepositories(excluded);
    chooser.setRepositories(GitRepositoryManager.getInstance(project).getRepositories());
    if (chooser.showAndGet()) {
      log.debug("Exclusions about to change");
      List<GitRepository> selectedRepositories = chooser.getSelectedRepositories();
      selectedRepositories = GtUtil.sort(selectedRepositories);
      List<String> selectedRoots = selectedRepositories.stream()
          .map(GitRepository::getRoot)
          .map(VirtualFile::getUrl)
          .collect(Collectors.toList());
      List<String> newContent = autoFetchExclusionsModel.toList();
      newContent.addAll(selectedRoots);
      log.debug("New exclusions: ", newContent);
      replaceAutoFetchExclusions(newContent);
    } else {
      log.debug("Exclusions change cancelled");
    }
  }

  private void onRemoveAutoFetchExclusion() {
    List<String> selectedValues = autoFetchExclusionsList.getSelectedValuesList();
    log.debug("Removing exclusions: ", selectedValues);
    selectedValues.forEach(autoFetchExclusionsModel::remove);
  }

  @Override
  public void afterStateSet() {
    updateCompletionItemActions();
  }

  public void afterInit() {
    autoFetchExclusionsList.setCellRenderer(new GitRepositoryRenderer(project));
    boolean defaultProject = project.isDefault();
    log.debug("Project.isDefault={}", defaultProject);
    if (defaultProject) {
      autoFetchExclusionsDecorator.disableAddAction();
      autoFetchExclusionsDecorator.disableRemoveAction();
      autoFetchExclusionsList.setEmptyText(ResBundle.getString("configurable.prj.default.na"));
    }
    autoFetchExclusionsPanel.add(autoFetchExclusionsDecorator.createPanel(), BorderLayout.CENTER);
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

  public void setAutoFetchExclusions(List<String> autoFetchExclusions) {
    replaceAutoFetchExclusions(autoFetchExclusions);
  }

  private void replaceAutoFetchExclusions(List<String> exclusions) {
    List<String> newContent = new ArrayList<>(exclusions);
    newContent.sort(String::compareTo);
    autoFetchExclusionsModel.replaceAll(newContent);
  }

  public List<String> getAutoFetchExclusions() {
    return autoFetchExclusionsModel.toList();
  }

  public void setProject(Project project) {
    this.project = project;
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

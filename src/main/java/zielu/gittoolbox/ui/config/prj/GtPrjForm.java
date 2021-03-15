package zielu.gittoolbox.ui.config.prj;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.ui.treeStructure.Tree;
import git4idea.repo.GitRepository;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.completion.FormatterIcons;
import zielu.gittoolbox.config.AutoFetchExclusionConfig;
import zielu.gittoolbox.config.CommitCompletionConfig;
import zielu.gittoolbox.config.CommitCompletionType;
import zielu.gittoolbox.config.ReferencePointForStatusConfig;
import zielu.gittoolbox.config.ReferencePointForStatusType;
import zielu.gittoolbox.config.RemoteConfig;
import zielu.gittoolbox.fetch.AutoFetchParams;
import zielu.gittoolbox.ui.config.CommitCompletionConfigFormData;
import zielu.gittoolbox.ui.config.GtPatternFormatterForm;
import zielu.gittoolbox.ui.config.ReferencePointForStatusTypeRenderer;
import zielu.gittoolbox.ui.config.common.AutoFetchExclusionTreeRenderer;
import zielu.gittoolbox.ui.config.common.AutoFetchExclusionsTreeModel;
import zielu.gittoolbox.ui.config.common.GtRemoteChooser;
import zielu.gittoolbox.ui.util.AppUiUtil;
import zielu.gittoolbox.util.GtUtil;
import zielu.intellij.ui.GtFormUi;

public class GtPrjForm implements GtFormUi {
  private final Logger log = Logger.getInstance(getClass());
  private final AutoFetchExclusionsTreeModel autoFetchExclusionsModel = new AutoFetchExclusionsTreeModel();
  private final Tree autoFetchExclusions = new Tree(autoFetchExclusionsModel);
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
  private ComboBox<ReferencePointForStatusType> referencePointTypeComboBox;
  private JTextField referencePointNameText;
  private JCheckBox autoFetchOnBranchSwitchEnabled;
  private JCheckBox commitMessageValidationCheckBox;
  private JTextField commitMessageValidationRegex;

  private GtPatternFormatterForm completionItemPatternForm;
  private Action addSimpleCompletionAction;
  private ToolbarDecorator autoFetchExclusionsDecorator;
  private Project project;

  @Override
  public void init() {
    addSimpleCompletionAction = new AbstractActionExt() {
      {
        setName(ResBundle.message("commit.dialog.completion.formatters.simple.add.label"));
        setSmallIcon(FormatterIcons.getSimple());
      }

      @Override
      public void actionPerformed(ActionEvent e) {
        completionItemModel.add(CommitCompletionConfig.createDefault(CommitCompletionType.SIMPLE));
        updateCompletionItemActions();
      }
    };
    addCommitCompletionPopup.add(addSimpleCompletionAction);
    Action addPatternCompletionAction = new AbstractActionExt() {
      {
        setName(ResBundle.message("commit.dialog.completion.formatters.pattern.add.label"));
        setSmallIcon(FormatterIcons.getRegExp());
      }

      @Override
      public void actionPerformed(ActionEvent e) {
        completionItemModel.add(CommitCompletionConfig.createDefault(CommitCompletionType.PATTERN));
        updateCompletionItemActions();
      }
    };
    addCommitCompletionPopup.add(addPatternCompletionAction);
    Action addIssuePatternCompletionAction = new AbstractActionExt() {
      {
        setName(ResBundle.message("commit.dialog.completion.formatters.pattern.issue.add.label"));
        setSmallIcon(FormatterIcons.getRegExp());
      }

      @Override
      public void actionPerformed(ActionEvent e) {
        completionItemModel.add(CommitCompletionConfig.createIssuePattern());
        updateCompletionItemActions();
      }
    };
    addCommitCompletionPopup.add(addIssuePatternCompletionAction);

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
    completionItemPatternForm.addPatternUpdate(text -> AppUiUtil.invokeLaterIfNeeded(completionItemList::repaint));
    ToolbarDecorator commitCompletionDecorator = ToolbarDecorator.createDecorator(completionItemList);
    commitCompletionDecorator.setAddAction(button -> {
      RelativePoint popupPoint = button.getPreferredPopupPoint();
      Point point = popupPoint.getPoint();
      addCommitCompletionPopup.show(popupPoint.getComponent(), point.x, point.y);
    });
    commitCompletionDecorator.setAddActionName(
        ResBundle.message("commit.dialog.completion.formatters.add.tooltip")
    );
    commitCompletionDecorator.setRemoveAction(button -> onCommitCompletionItemRemove());
    commitCompletionDecorator.setRemoveActionName(
        ResBundle.message("commit.dialog.completion.formatters.remove.tooltip")
    );
    commitCompletionPanel.add(commitCompletionDecorator.createPanel(), BorderLayout.CENTER);

    autoFetchExclusions.setRootVisible(false);
    autoFetchExclusions.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    autoFetchExclusionsDecorator = ToolbarDecorator.createDecorator(autoFetchExclusions);
    autoFetchExclusionsDecorator.setAddAction(button -> onAddAutoFetchExclusion());
    autoFetchExclusionsDecorator.setAddActionName(
        ResBundle.message("configurable.prj.autoFetch.exclusions.add.label"));
    autoFetchExclusionsDecorator.setRemoveAction(button -> onRemoveAutoFetchExclusion());
    autoFetchExclusionsDecorator.setRemoveActionName(
        ResBundle.message("configurable.prj.autoFetch.exclusions.remove.label"));
    CollectionComboBoxModel<ReferencePointForStatusType> referencePointTypeModel =
        new CollectionComboBoxModel<>(ReferencePointForStatusType.allValues());
    referencePointTypeComboBox.setModel(referencePointTypeModel);
    referencePointTypeComboBox.setRenderer(new ReferencePointForStatusTypeRenderer());
    referencePointTypeComboBox.addActionListener(e -> {
      boolean parentBranch = getReferencePointType() == ReferencePointForStatusType.SELECTED_PARENT_BRANCH;
      referencePointNameText.setEnabled(parentBranch);
    });
  }

  private void onCompletionItemSelected(CommitCompletionConfig config) {
    if (config == null) {
      completionItemPatternForm.getContent().setVisible(false);
    } else if (config.getType() == CommitCompletionType.PATTERN) {
      completionItemPatternForm.setData(new CommitCompletionConfigFormData(config));
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
    return CommitCompletionConfig.createDefault(CommitCompletionType.SIMPLE);
  }

  private void onCommitCompletionItemRemove() {
    int selectedIndex = completionItemList.getSelectionModel().getMinSelectionIndex();
    if (selectedIndex > -1) {
      completionItemModel.removeRow(selectedIndex);
      updateCompletionItemActions();
    }
  }

  private void onAddAutoFetchExclusion() {
    TreePath selectionPath = autoFetchExclusions.getSelectionPath();
    if (selectionPath == null || autoFetchExclusionsModel.hasRemoteAt(selectionPath)) {
      addAutoFetchExclusionRepo();
    } else if (autoFetchExclusionsModel.hasConfigAt(selectionPath)) {
      AutoFetchExclusionConfig config = autoFetchExclusionsModel.getConfigAt(selectionPath);
      if (config != null && addAutoFetchExclusionRemote(config)) {
        autoFetchExclusionsModel.setConfigs(autoFetchExclusionsModel.getConfigs());
        autoFetchExclusions.expandPath(selectionPath);
      }
    }
  }

  private void addAutoFetchExclusionRepo() {
    log.debug("Add exclusions...");
    GtRepoChooser chooser = new GtRepoChooser(project, content);
    List<GitRepository> excluded = getExcludedRepositories(autoFetchExclusionsModel.getConfigs());
    log.debug("Currently excluded: ", excluded);
    chooser.setSelectedRepositories(excluded);
    chooser.setRepositories(GtUtil.getRepositories(project));
    if (chooser.showAndGet()) {
      log.debug("Exclusions about to change");
      List<GitRepository> selectedRepositories = chooser.getSelectedRepositories();
      selectedRepositories = GtUtil.sort(selectedRepositories);
      List<AutoFetchExclusionConfig> selectedRoots = selectedRepositories.stream()
                                                         .map(GitRepository::getRoot)
                                                         .map(VirtualFile::getUrl)
                                                         .map(AutoFetchExclusionConfig::new)
                                                         .collect(Collectors.toList());
      autoFetchExclusionsModel.addConfigs(selectedRoots);
    } else {
      log.debug("Exclusions change cancelled");
    }
  }

  private boolean addAutoFetchExclusionRemote(AutoFetchExclusionConfig config) {
    Optional<GitRepository> maybeRepository = VirtualFileRepoCache.getInstance(project)
            .findRepoForRoot(config.getRepositoryRootPath());
    if (maybeRepository.isPresent()) {
      GtRemoteChooser chooser = new GtRemoteChooser(project, content);
      GitRepository repository = maybeRepository.get();
      chooser.setRepositoryName(GtUtil.name(repository));
      chooser.setRemotes(repository.getRemotes());
      chooser.setSelectedRemotes(config.getExcludedRemotes().stream()
                                     .map(RemoteConfig::getName)
                                     .collect(Collectors.toList()));
      if (chooser.showAndGet()) {
        List<String> selectedRemotes = chooser.getSelectedRemotes();
        config.setExcludedRemotes(selectedRemotes.stream().map(RemoteConfig::new).collect(Collectors.toList()));
        return true;
      }
    }
    return false;
  }

  private List<GitRepository> getExcludedRepositories(Collection<AutoFetchExclusionConfig> exclusions) {
    List<String> roots = exclusions.stream()
        .map(AutoFetchExclusionConfig::getRepositoryRootPath)
        .collect(Collectors.toList());
    return VirtualFileRepoCache.getInstance(project).findReposForRoots(roots);
  }

  private void onRemoveAutoFetchExclusion() {
    TreePath selectionPath = autoFetchExclusions.getSelectionPath();
    autoFetchExclusionsModel.removeAtPath(selectionPath);
  }

  @Override
  public void afterStateSet() {
    updateCompletionItemActions();
  }

  public void afterInit() {
    autoFetchExclusions.setCellRenderer(new AutoFetchExclusionTreeRenderer(project));
    boolean defaultProject = project.isDefault();
    log.debug("Project.isDefault={}", defaultProject);
    if (defaultProject) {
      autoFetchExclusionsDecorator.disableAddAction();
      autoFetchExclusionsDecorator.disableRemoveAction();
    }
    autoFetchExclusionsPanel.add(autoFetchExclusionsDecorator.createPanel(), BorderLayout.CENTER);
  }

  @Override
  public void dispose() {
    completionItemPatternForm.dispose();
  }

  public boolean getCommitMessageValidationEnabled() {
    return commitMessageValidationCheckBox.isSelected();
  }

  public void setCommitMessageValidationEnabled(boolean enabled) {
    commitMessageValidationCheckBox.setSelected(enabled);
  }

  public String getCommitMessageValidationRegex() {
    return commitMessageValidationRegex.getText();
  }

  public void setCommitMessageValidationRegex(String regex) {
    commitMessageValidationRegex.setText(regex);
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

  public void setAutoFetchExclusions(List<AutoFetchExclusionConfig> autoFetchExclusions) {
    autoFetchExclusionsModel.setConfigs(autoFetchExclusions);
  }

  public List<AutoFetchExclusionConfig> getAutoFetchExclusions() {
    return autoFetchExclusionsModel.getConfigs();
  }

  void setAutoFetchOnBranchSwitchEnabled(boolean enabled) {
    autoFetchOnBranchSwitchEnabled.setSelected(enabled);
  }

  boolean getAutoFetchOnBranchSwitchEnabled() {
    return autoFetchOnBranchSwitchEnabled.isSelected();
  }

  public void setReferencePointConfig(ReferencePointForStatusConfig config) {
    referencePointTypeComboBox.setSelectedItem(config.getType());
    referencePointNameText.setText(config.getName());
  }

  public ReferencePointForStatusConfig getReferencePointConfig() {
    ReferencePointForStatusType type = getReferencePointType();
    String name = referencePointNameText.getText();
    return new ReferencePointForStatusConfig(type, name);
  }

  private ReferencePointForStatusType getReferencePointType() {
    return (ReferencePointForStatusType) referencePointTypeComboBox.getSelectedItem();
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
      completionIcons.put(CommitCompletionType.SIMPLE, FormatterIcons.getSimple());
      completionIcons.put(CommitCompletionType.PATTERN, FormatterIcons.getRegExp());
    }

    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends CommitCompletionConfig> list,
                                         CommitCompletionConfig value, int index, boolean selected, boolean hasFocus) {
      setIcon(getIconForCompletion(value));
      append(value.getPresentableText());
    }

    private Icon getIconForCompletion(CommitCompletionConfig config) {
      return completionIcons.get(config.getType());
    }
  }
}

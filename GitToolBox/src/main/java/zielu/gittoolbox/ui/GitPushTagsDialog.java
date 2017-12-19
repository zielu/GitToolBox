package zielu.gittoolbox.ui;

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.CheckBoxListListener;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import git4idea.GitLocalBranch;
import git4idea.GitUtil;
import git4idea.repo.GitBranchTrackInfo;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.tag.GitTagCalculator;
import zielu.gittoolbox.tag.TagsPushSpec;
import zielu.gittoolbox.tag.TagsPushSpec.Builder;

public class GitPushTagsDialog extends DialogWrapper {
  private final LinkedList<String> existingTags = Lists.newLinkedList();
  private final Project project;
  private final GitTagCalculator tagCalculator;
  private JPanel panel;
  private ComboBox gitRootComboBox;
  private JLabel currentBranch;
  private CheckBoxList<String> tagsList;
  private JLabel selectedCountLabel;
  private JBCheckBox forceCheckbox;
  private int selectedCount;

  public GitPushTagsDialog(Project project, List<VirtualFile> roots, VirtualFile defaultRoot) {
    super(project, true);
    this.project = project;
    tagCalculator = GitTagCalculator.create(this.project);
    initGui();
    GitUIUtil.setupRootChooser(this.project, roots, defaultRoot, gitRootComboBox, null);
    updateRepositoryState();
    init();
  }

  private void initGui() {
    setTitle(ResBundle.getString("push.tags.title"));
    setOKButtonText(ResBundle.getString("push.tags.ok.button"));
    panel = new JPanel(new MigLayout("fill, top, insets 0", "[]10[grow, fill]"));
    gitRootComboBox = new ComboBox();
    gitRootComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateRepositoryState();
      }
    });
    panel.add(new JBLabel(ResBundle.getString("git.root")));
    panel.add(gitRootComboBox, "spanx, wrap");
    currentBranch = new JBLabel();
    panel.add(new JBLabel(ResBundle.getString("current.branch")));
    panel.add(currentBranch, "spanx, wrap");
    panel.add(new JBLabel(ResBundle.getString("force.tags.push.label")));
    forceCheckbox = new JBCheckBox(ResBundle.getString("force.tags.push.text"));
    panel.add(forceCheckbox, "spanx, wrap");
    tagsList = new CheckBoxList<String>(new CheckBoxListListener() {
      @Override
      public void checkBoxSelectionChanged(int index, boolean value) {
        if (value) {
          selectedCount++;
        } else {
          selectedCount--;
        }
        updateSelectedCount();
      }
    });
    panel.add(ScrollPaneFactory.createScrollPane(tagsList), "gaptop 10, span, grow, push, wrap");
    JPanel selectPanel = new JPanel(new MigLayout("fill, insets 0", "[]5[]10[grow]"));
    panel.add(selectPanel, "spanx, growx, pushx, wrap");
    selectPanel.add(new LinkLabel(ResBundle.getString("select.all"), null, new LinkListener() {
      @Override
      public void linkSelected(LinkLabel source, Object linkData) {
        changeItemsSelection(true);
      }
    }));
    selectPanel.add(new LinkLabel(ResBundle.getString("select.none"), null, new LinkListener() {
      @Override
      public void linkSelected(LinkLabel source, Object linkData) {
        changeItemsSelection(false);
      }
    }));
    selectedCountLabel = new JBLabel();
    selectPanel.add(selectedCountLabel, "spanx, pushx, right");
    getOKAction().putValue(DEFAULT_ACTION, true);
  }

  private void changeItemsSelection(boolean selected) {
    for (String tag : existingTags) {
      tagsList.setItemSelected(tag, selected);
    }
    tagsList.repaint();
    selectedCount = selected ? existingTags.size() : 0;
    updateSelectedCount();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return panel;
  }

  private VirtualFile getGitRoot() {
    return (VirtualFile) gitRootComboBox.getSelectedItem();
  }

  @Nullable
  @Override
  protected String getDimensionServiceKey() {
    return getClass().getName();
  }

  private int countFormatWidth() {
    return existingTags.isEmpty() ? 1 : String.valueOf(existingTags.size()).length();
  }

  private String makeCountFormat() {
    int width = countFormatWidth();
    return "%1$" + width + "d/%2$" + width + "d";
  }

  private String makeSelectedCountMessage() {
    String format = makeCountFormat();
    return String.format(format, selectedCount, existingTags.size());
  }

  private void updateSelectedCount() {
    String message = makeSelectedCountMessage();
    selectedCountLabel.setText(message);
  }

  private void fetchTags() {
    existingTags.clear();
    Optional<GitLocalBranch> current = currentBranch();
    if (current.isPresent()) {
      List<String> newTags = tagCalculator.tagsForBranch(getGitRoot(), current.get().getName());
      existingTags.addAll(newTags);
    }
  }

  private void updateRepositoryState() {
    fetchTags();
    tagsList.setStringItems(Maps.toMap(existingTags, Functions.constant(true)));
    selectedCount = existingTags.size();
    updateCurrentBranch();
    updateSelectedCount();
    validatePushAvailable();
  }

  private void updateCurrentBranch() {
    Optional<GitLocalBranch> current = currentBranch();
    if (current.isPresent()) {
      Optional<GitBranchTrackInfo> remote = remoteForCurrentBranch();
      if (remote.isPresent()) {
        currentBranch.setText(remote.get().toString());
      } else {
        currentBranch.setText(current.get().getName());
      }
    } else {
      currentBranch.setText(GitUIUtil.NO_CURRENT_BRANCH);
    }
  }

  private Optional<GitLocalBranch> currentBranch() {
    GitRepository repository = getRepository();
    return Optional.ofNullable(repository.getCurrentBranch());
  }

  private GitRepository getRepository() {
    return Preconditions.checkNotNull(GitUtil.getRepositoryManager(project).getRepositoryForRoot(getGitRoot()));
  }

  private Optional<GitBranchTrackInfo> remoteForCurrentBranch() {
    GitRepository repository = getRepository();
    return Optional.ofNullable(GitUtil.getTrackInfoForCurrentBranch(repository));
  }

  private void validatePushAvailable() {
    Optional<GitBranchTrackInfo> tracking = remoteForCurrentBranch();
    calculateOkActionState(tracking);
    if (tracking.isPresent()) {
      setErrorText(null);
    } else {
      setErrorText(ResBundle.getString("message.cannot.push.without.tracking"));
    }
  }

  private void calculateOkActionState(Optional<GitBranchTrackInfo> tracking) {
    getOKAction().setEnabled(tracking.isPresent() && selectedCount > 0);
  }

  private ImmutableList<String> getSelectedTags() {
    return ImmutableList.copyOf(Collections2.filter(existingTags, new Predicate<String>() {
      @Override
      public boolean apply(String tag) {
        return tagsList.isItemSelected(tag);
      }
    }));
  }

  private boolean isAnythingSelected() {
    return selectedCount > 0;
  }

  private Builder pushSpecBuilder() {
    Builder builder = TagsPushSpec.builder();
    if (selectedCount != existingTags.size()) {
      builder.tags(getSelectedTags());
    }
    if (forceCheckbox.isSelected()) {
      builder.force();
    }
    return builder;
  }

  public java.util.Optional<TagsPushSpec> getPushSpec() {
    if (isAnythingSelected()) {
      return Optional.of(pushSpecBuilder().build(getGitRoot()));
    } else {
      return Optional.empty();
    }
  }
}

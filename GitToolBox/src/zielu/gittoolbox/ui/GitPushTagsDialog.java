package zielu.gittoolbox.ui;

import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
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
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import com.intellij.util.ui.RadioButtonEnumModel;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ButtonGroup;
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
    private JPanel myPanel;
    private ComboBox myGitRootComboBox;
    private JLabel myCurrentBranch;
    private CheckBoxList<String> myTagsList;
    private JLabel mySelectedCountLabel;
    private RadioButtonEnumModel<VisibleTags> myFilterModel;
    private JBCheckBox myForceCheckbox;

    private final LinkedList<String> myExistingTags = Lists.newLinkedList();
    private final Project myProject;
    private final GitTagCalculator myTagCalculator;
    private int mySelectedCount;

    public GitPushTagsDialog(Project project, List<VirtualFile> roots, VirtualFile defaultRoot) {
        super(project, true);
        myProject = project;
        myTagCalculator = GitTagCalculator.create(myProject);
        initGui();
        GitUIUtil.setupRootChooser(myProject, roots, defaultRoot, myGitRootComboBox, myCurrentBranch);
        updateTags();
        init();
    }

    private void initGui() {
        setTitle(ResBundle.getString("push.tags.title"));
        setOKButtonText(ResBundle.getString("push.tags.ok.button"));
        myPanel = new JPanel(new MigLayout("fill, top, insets 0", "[]10[grow, fill]"));
        myGitRootComboBox = new ComboBox();
        myGitRootComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTags();
            }
        });
        myPanel.add(new JBLabel(ResBundle.getString("git.root")));
        myPanel.add(myGitRootComboBox, "spanx, wrap");
        myCurrentBranch = new JBLabel();
        myPanel.add(new JBLabel(ResBundle.getString("current.branch")));
        myPanel.add(myCurrentBranch, "spanx, wrap");
        myPanel.add(new JBLabel(ResBundle.getString("force.tags.push.label")));
        myForceCheckbox = new JBCheckBox(ResBundle.getString("force.tags.push.text"));
        myPanel.add(myForceCheckbox, "spanx, wrap");
        myPanel.add(new JBLabel(ResBundle.getString("show.tags")));
        JPanel filterPanel = new JPanel(new MigLayout("insets 0"));
        myPanel.add(filterPanel, "spanx, growx, pushx, wrap");
        JBRadioButton showOnBranch = new JBRadioButton(ResBundle.getString("show.tags.on.branch"));
        JBRadioButton showAll = new JBRadioButton(ResBundle.getString("show.tags.all"));
        filterPanel.add(showOnBranch);
        filterPanel.add(showAll);
        ButtonGroup filterGroup = new ButtonGroup();
        filterGroup.add(showOnBranch);
        filterGroup.add(showAll);
        myFilterModel = RadioButtonEnumModel.bindEnum(VisibleTags.class, filterGroup);
        myFilterModel.setSelected(VisibleTags.onBranch);
        myFilterModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTags();
            }
        });
        myTagsList = new CheckBoxList<String>(new CheckBoxListListener() {
            @Override
            public void checkBoxSelectionChanged(int index, boolean value) {
                if (value) {
                    mySelectedCount++;
                } else {
                    mySelectedCount--;
                }
                updateSelectedCount();
            }
        });
        myPanel.add(ScrollPaneFactory.createScrollPane(myTagsList), "gaptop 10, span, grow, push, wrap");
        JPanel selectPanel = new JPanel(new MigLayout("fill, insets 0", "[]5[]10[grow]"));
        myPanel.add(selectPanel, "spanx, growx, pushx, wrap");
        selectPanel.add(new LinkLabel(ResBundle.getString("select.all"), null, new LinkListener() {
            @Override
            public void linkSelected(LinkLabel aSource, Object aLinkData) {
                changeItemsSelection(true);
            }
        }));
        selectPanel.add(new LinkLabel(ResBundle.getString("select.none"), null, new LinkListener() {
            @Override
            public void linkSelected(LinkLabel aSource, Object aLinkData) {
                changeItemsSelection(false);
            }
        }));
        mySelectedCountLabel = new JBLabel();
        selectPanel.add(mySelectedCountLabel, "spanx, pushx, right");
        getOKAction().putValue(DEFAULT_ACTION, true);
    }

    private void changeItemsSelection(boolean selected) {
        for (String tag : myExistingTags) {
            myTagsList.setItemSelected(tag, selected);
        }
        myTagsList.repaint();
        mySelectedCount = selected ? myExistingTags.size() : 0;
        updateSelectedCount();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myPanel;
    }

    private VirtualFile getGitRoot() {
        return (VirtualFile) myGitRootComboBox.getSelectedItem();
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return getClass().getName();
    }

    private int countFormatWidth() {
        return myExistingTags.isEmpty() ? 1 : String.valueOf(myExistingTags.size()).length();
    }

    private String makeCountFormat() {
        int width = countFormatWidth();
        return "%1$" + width + "d/%2$" + width + "d";
    }

    private String makeSelectedCountMessage() {
        String format = makeCountFormat();
        return String.format(format, mySelectedCount, myExistingTags.size());
    }

    private void updateSelectedCount() {
        String message = makeSelectedCountMessage();
        mySelectedCountLabel.setText(message);
        getOKAction().setEnabled(mySelectedCount > 0);
    }

    private boolean hasCurrentBranch() {
        String name = myCurrentBranch.getText();
        return !Strings.isNullOrEmpty(name) && !GitUIUtil.NO_CURRENT_BRANCH.equals(name);
    }

    private void fetchTags() {
        myExistingTags.clear();
        if (hasCurrentBranch()) {
            List<String> newTags;
            switch (myFilterModel.getSelected()) {
                case all: {
                    newTags = myTagCalculator.allTags(getGitRoot());
                    break;
                }
                case onBranch: {
                    newTags = myTagCalculator.tagsForBranch(getGitRoot(), myCurrentBranch.getText());
                    break;
                }
                default: {
                    newTags = Collections.emptyList();
                }
            }
            myExistingTags.addAll(newTags);
        }
    }

    private void updateTags() {
        fetchTags();
        myTagsList.setStringItems(Maps.toMap(myExistingTags, Functions.constant(true)));
        mySelectedCount = myExistingTags.size();
        updateSelectedCount();
        validatePushAvailable();
    }

    private void validatePushAvailable() {
        GitRepository repository = Preconditions.checkNotNull(GitUtil.getRepositoryManager(myProject).getRepositoryForRoot(getGitRoot()));
        boolean remoteBranchSet = GitUtil.getTrackInfoForCurrentBranch(repository) != null;
        getOKAction().setEnabled(remoteBranchSet);
        if (remoteBranchSet) {
            setErrorText(null);
        } else {
            setErrorText(ResBundle.getString("message.cannot.push.without.tracking"));
        }
    }

    private ImmutableList<String> getSelectedTags() {
        return ImmutableList.copyOf(Collections2.filter(myExistingTags, new Predicate<String>() {
            @Override
            public boolean apply(String tag) {
                return myTagsList.isItemSelected(tag);
            }
        }));
    }

    private boolean isAnythingSelected() {
        return mySelectedCount > 0;
    }

    private Builder pushSpecBuilder() {
        Builder builder = TagsPushSpec.builder();
        switch (myFilterModel.getSelected()) {
            case all: {
                if (mySelectedCount != myExistingTags.size()) {
                    builder.tags(getSelectedTags());
                }
                break;
            }
            case onBranch: {
                builder.tags(getSelectedTags());
                break;
            }
        }
        if (myForceCheckbox.isSelected()) {
            builder.force();
        }
        return builder;
    }

    public Optional<TagsPushSpec> getPushSpec() {
        if (isAnythingSelected()) {
            return Optional.of(pushSpecBuilder().build(getGitRoot()));
        } else {
            return Optional.absent();
        }
    }

    private enum VisibleTags {
        onBranch,
        all
    }
}

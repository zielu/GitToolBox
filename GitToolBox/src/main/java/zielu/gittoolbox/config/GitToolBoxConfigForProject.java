package zielu.gittoolbox.config;

import com.google.common.collect.Lists;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.fetch.AutoFetchParams;
import zielu.gittoolbox.formatter.Formatter;
import zielu.gittoolbox.util.AppUtil;

@State(
    name = "GitToolBoxProjectSettings",
    storages = @Storage("git_toolbox_prj.xml")
)
public class GitToolBoxConfigForProject implements PersistentStateComponent<GitToolBoxConfigForProject> {
  public boolean autoFetch = true;
  public int autoFetchIntervalMinutes = AutoFetchParams.DEFAULT_INTERVAL_MINUTES;
  public List<String> autoFetchExclusions = new ArrayList<>();
  public boolean autoFetchOnBranchSwitch = true;
  public boolean commitDialogCompletion = true;
  public List<CommitCompletionConfig> completionConfigs = Lists.newArrayList(new CommitCompletionConfig());
  public ReferencePointForStatusConfig referencePointForStatus = new ReferencePointForStatusConfig();

  public static GitToolBoxConfigForProject getInstance(Project project) {
    return AppUtil.getServiceInstance(project, GitToolBoxConfigForProject.class);
  }

  public GitToolBoxConfigForProject copy() {
    GitToolBoxConfigForProject copy = new GitToolBoxConfigForProject();
    copy.autoFetch = autoFetch;
    copy.autoFetchIntervalMinutes = autoFetchIntervalMinutes;
    copy.autoFetchExclusions = new ArrayList<>(autoFetchExclusions);
    copy.autoFetchOnBranchSwitch = autoFetchOnBranchSwitch;
    copy.commitDialogCompletion = commitDialogCompletion;
    copy.completionConfigs = completionConfigs.stream().map(CommitCompletionConfig::copy).collect(Collectors.toList());
    copy.referencePointForStatus = referencePointForStatus.copy();
    return copy;
  }

  public boolean isAutoFetchChanged(boolean autoFetch) {
    return this.autoFetch != autoFetch;
  }

  public boolean isAutoFetchIntervalMinutesChanged(int autoFetchIntervalMinutes) {
    return this.autoFetchIntervalMinutes != autoFetchIntervalMinutes;
  }

  public boolean isCommitDialogCompletionChanged(boolean commitDialogCompletion) {
    return this.commitDialogCompletion != commitDialogCompletion;
  }

  public boolean isCommitDialogCompletionConfigsChanged(List<CommitCompletionConfig> completionConfigs) {
    return !this.completionConfigs.equals(completionConfigs);
  }

  public boolean isAutoFetchExclusionsChanged(List<String> autoFetchExclusions) {
    return !this.autoFetchExclusions.equals(autoFetchExclusions);
  }

  public boolean isAutoFetchOnBranchSwitchChanged(boolean autoFetchOnBranchSwitch) {
    return this.autoFetchOnBranchSwitch != autoFetchOnBranchSwitch;
  }

  public boolean isReferencePointForStatusChanged(ReferencePointForStatusConfig config) {
    return this.referencePointForStatus.isChanged(config);
  }

  public void fireChanged(@NotNull Project project, @NotNull GitToolBoxConfigForProject previous) {
    project.getMessageBus().syncPublisher(ConfigNotifier.CONFIG_TOPIC)
        .configChanged(project, previous, this);
  }

  @Transient
  public List<Formatter> getCompletionFormatters() {
    return completionConfigs.stream().map(CommitCompletionConfig::createFormatter).collect(Collectors.toList());
  }

  @Nullable
  @Override
  public GitToolBoxConfigForProject getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull GitToolBoxConfigForProject state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}

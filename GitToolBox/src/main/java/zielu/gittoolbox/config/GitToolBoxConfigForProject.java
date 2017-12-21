package zielu.gittoolbox.config;

import com.google.common.collect.Lists;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.fetch.AutoFetchParams;
import zielu.gittoolbox.fetch.AutoFetchStrategy;
import zielu.gittoolbox.formatter.Formatter;

@State(
    name = "GitToolBoxProjectSettings",
    storages = @Storage("git_toolbox_prj.xml")
)
public class GitToolBoxConfigForProject implements PersistentStateComponent<GitToolBoxConfigForProject> {
  public boolean autoFetch = true;
  public int autoFetchIntervalMinutes = AutoFetchParams.DEFAULT_INTERVAL_MINUTES;
  public String autoFetchStrategy = AutoFetchStrategy.REPO_WITH_REMOTES.key();
  public boolean commitDialogCompletion = true;
  public List<CommitCompletionConfig> completionConfigs = Lists.newArrayList(new CommitCompletionConfig());

  public static GitToolBoxConfigForProject getInstance(Project project) {
    return ServiceManager.getService(project, GitToolBoxConfigForProject.class);
  }

  @Transient
  public AutoFetchStrategy getAutoFetchStrategy() {
    return AutoFetchStrategy.forKey(autoFetchStrategy);
  }

  public void setAutoFetchStrategy(AutoFetchStrategy strategy) {
    autoFetchStrategy = strategy.key();
  }

  public boolean isAutoFetchStrategyChanged(AutoFetchStrategy strategy) {
    return !autoFetchStrategy.equals(strategy.key());
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

  @SuppressFBWarnings({"NP_NULL_ON_SOME_PATH"})
  public void fireChanged(@NotNull Project project) {
    project.getMessageBus().syncPublisher(ConfigNotifier.CONFIG_TOPIC).configChanged(project, this);
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
  public void loadState(GitToolBoxConfigForProject state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}

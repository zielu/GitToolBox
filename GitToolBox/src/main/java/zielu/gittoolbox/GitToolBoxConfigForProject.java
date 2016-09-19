package zielu.gittoolbox;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.fetch.AutoFetchParams;
import zielu.gittoolbox.fetch.AutoFetchStrategy;

@State(
    name = "GitToolBoxProjectSettings",
    storages = @Storage("git_toolbox_prj.xml")
)
public class GitToolBoxConfigForProject implements PersistentStateComponent<GitToolBoxConfigForProject> {
    public boolean autoFetch = true;
    public int autoFetchIntervalMinutes = AutoFetchParams.defaultIntervalMinutes;
    public String autoFetchStrategy = AutoFetchStrategy.RepoWithRemotes.key();

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

    public void fireChanged(@NotNull Project project) {
        project.getMessageBus().syncPublisher(ConfigNotifier.CONFIG_TOPIC).configChanged(project, this);
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

    public static GitToolBoxConfigForProject getInstance(Project project) {
        return ServiceManager.getService(project, GitToolBoxConfigForProject.class);
    }
}

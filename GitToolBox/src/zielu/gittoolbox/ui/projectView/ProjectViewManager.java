package zielu.gittoolbox.ui.projectView;

import com.google.common.base.Optional;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.GitToolBoxConfigNotifier;
import zielu.gittoolbox.cache.PerRepoStatusCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.status.GitAheadBehindCount;

public class ProjectViewManager implements Disposable {
    private final Project myProject;
    private final MessageBusConnection myConnection;

    private ProjectViewManager(Project project) {
        this.myProject = project;
        myConnection = myProject.getMessageBus().connect(this);
        myConnection.subscribe(GitToolBoxConfigNotifier.CONFIG_TOPIC, new GitToolBoxConfigNotifier() {
            @Override
            public void configChanged(GitToolBoxConfig config) {
                refreshProjectView();
            }
        });
        myConnection.subscribe(PerRepoStatusCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
            @Override
            public void stateChanged(@NotNull final Optional<GitAheadBehindCount> aheadBehind,
                                     @NotNull final GitRepository repository) {
                refreshProjectView();
            }
        });
    }

    private void refreshProjectView() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ProjectView.getInstance(myProject).refresh();
            }
        });
    }

    public static ProjectViewManager create(Project project) {
        return new ProjectViewManager(project);
    }

    public void opened() {

    }

    @Override
    public void dispose() {
        myConnection.disconnect();
    }
}

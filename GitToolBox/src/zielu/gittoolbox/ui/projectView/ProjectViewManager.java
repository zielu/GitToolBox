package zielu.gittoolbox.ui.projectView;

import com.google.common.base.Optional;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import git4idea.repo.GitRepository;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.GitToolBoxConfigNotifier;
import zielu.gittoolbox.cache.PerRepoStatusCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.status.GitAheadBehindCount;

public class ProjectViewManager implements Disposable {
    private final AtomicBoolean opened = new AtomicBoolean();
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
        if (opened.get()) {
            UIUtil.invokeLaterIfNeeded(new Runnable() {
                @Override
                public void run() {
                    if (opened.get()) {
                        ProjectView.getInstance(myProject).refresh();
                    }
                }
            });
        }
    }

    public static ProjectViewManager create(Project project) {
        return new ProjectViewManager(project);
    }

    public void opened() {
        opened.compareAndSet(false, true);
    }

    public void closed() {
        opened.compareAndSet(true, false);
    }

    @Override
    public void dispose() {
        myConnection.disconnect();
    }
}

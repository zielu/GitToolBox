package zielu.gittoolbox;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.ui.projectView.ProjectViewManager;
import zielu.gittoolbox.ui.statusBar.StatusBarManager;
import zielu.gittoolbox.util.ProjectAwares;

public class GitToolBoxProject extends AbstractProjectComponent {
    private final Logger LOG = Logger.getInstance(getClass());
    private PerRepoInfoCache perRepoInfoCache;
    private StatusBarManager myStatusBarManager;
    private ProjectViewManager myProjectViewManager;
    private ProjectAwares myAwares;

    public GitToolBoxProject(@NotNull Project project) {
        super(project);
    }

    public static GitToolBoxProject getInstance(@NotNull Project project) {
        return project.getComponent(GitToolBoxProject.class);
    }

    @Override
    public void initComponent() {
        perRepoInfoCache = PerRepoInfoCache.create(myProject);
        myStatusBarManager = StatusBarManager.create(myProject);
        myProjectViewManager = ProjectViewManager.create(myProject);
        myAwares = ProjectAwares.create(
            perRepoInfoCache,
            myStatusBarManager,
            myProjectViewManager
        );
    }

    @Override
    public void disposeComponent() {
        perRepoInfoCache.dispose();
        myStatusBarManager.dispose();
        myProjectViewManager.dispose();
        myAwares.dispose();
    }

    public PerRepoInfoCache perRepoStatusCache() {
        return perRepoInfoCache;
    }

    @Override
    public void projectOpened() {
        myAwares.opened();
        LOG.debug("Project opened");
    }

    @Override
    public void projectClosed() {
        myAwares.closed();
        LOG.debug("Project closed");
    }
}

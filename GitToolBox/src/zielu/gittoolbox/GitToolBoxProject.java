package zielu.gittoolbox;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.fetch.AutoFetch;
import zielu.gittoolbox.ui.projectView.ProjectViewManager;
import zielu.gittoolbox.ui.statusBar.StatusBarManager;
import zielu.gittoolbox.util.ProjectAwares;

public class GitToolBoxProject extends AbstractProjectComponent {
    private final Logger LOG = Logger.getInstance(getClass());
    private PerRepoInfoCache perRepoInfoCache;
    private StatusBarManager myStatusBarManager;
    private ProjectViewManager myProjectViewManager;
    private AutoFetch myAutoFetch;
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
        myAutoFetch = AutoFetch.create(myProject);
        myAwares = ProjectAwares.create(
            perRepoInfoCache,
            myStatusBarManager,
            myProjectViewManager,
            myAutoFetch
        );
    }

    @Override
    public void disposeComponent() {
        perRepoInfoCache.dispose();
        myStatusBarManager.dispose();
        myProjectViewManager.dispose();
        myAutoFetch.dispose();
        myAwares.dispose();
    }

    public PerRepoInfoCache perRepoStatusCache() {
        return perRepoInfoCache;
    }

    public AutoFetch autoFetch() {
        return myAutoFetch;
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

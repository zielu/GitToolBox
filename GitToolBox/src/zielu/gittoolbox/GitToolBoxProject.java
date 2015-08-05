package zielu.gittoolbox;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.PerRepoStatusCache;
import zielu.gittoolbox.ui.StatusBarManager;
import zielu.gittoolbox.ui.projectView.ProjectViewManager;

public class GitToolBoxProject extends AbstractProjectComponent {
    private final Logger LOG = Logger.getInstance(getClass());
    private PerRepoStatusCache perRepoStatusCache;
    private StatusBarManager myStatusBarManager;
    private ProjectViewManager myProjectViewManager;

    public GitToolBoxProject(@NotNull Project project) {
        super(project);
    }

    public static GitToolBoxProject getInstance(@NotNull Project project) {
        return project.getComponent(GitToolBoxProject.class);
    }

    @Override
    public void initComponent() {
        perRepoStatusCache = PerRepoStatusCache.create(myProject);
        myStatusBarManager = StatusBarManager.create(myProject);
        myProjectViewManager = ProjectViewManager.create(myProject);
    }

    @Override
    public void disposeComponent() {
        perRepoStatusCache.dispose();
        myStatusBarManager.dispose();
        myProjectViewManager.dispose();
    }

    public PerRepoStatusCache perRepoStatusCache() {
        return perRepoStatusCache;
    }

    @Override
    public void projectOpened() {
        myStatusBarManager.opened();
        myProjectViewManager.opened();
        LOG.debug("Project opened");
    }

    @Override
    public void projectClosed() {
        myStatusBarManager.closed();
        myProjectViewManager.closed();
    }
}

package zielu.gittoolbox;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.PerRepoStatusCache;
import zielu.gittoolbox.ui.GitStatusWidget;

public class GitToolBoxProject extends AbstractProjectComponent {
    private final Logger LOG = Logger.getInstance(getClass());
    private PerRepoStatusCache perRepoStatusCache;

    private StatusBarWidget myStatusWidget;

    public GitToolBoxProject(@NotNull Project project) {
        super(project);
    }

    public static GitToolBoxProject getInstance(@NotNull Project project) {
        return project.getComponent(GitToolBoxProject.class);
    }

    @Override
    public void initComponent() {
        perRepoStatusCache = PerRepoStatusCache.create(myProject);
    }

    @Override
    public void disposeComponent() {
        perRepoStatusCache.dispose();
    }

    public PerRepoStatusCache perRepoStatusCache() {
        return perRepoStatusCache;
    }

    @Override
    public void projectOpened() {
        if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
            myStatusWidget = GitStatusWidget.create(myProject);
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
            if (statusBar != null) {
                statusBar.addWidget(myStatusWidget, myProject);
            }
        }
        LOG.debug("Project opened");
    }

    @Override
    public void projectClosed() {
        if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
            if (myStatusWidget != null) {
                StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
                if (statusBar != null) {
                    statusBar.removeWidget(myStatusWidget.ID());
                }
            }
        }
    }
}

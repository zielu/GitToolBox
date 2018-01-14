package zielu.gittoolbox;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoInfoCacheImpl;
import zielu.gittoolbox.ui.projectview.ProjectViewManager;
import zielu.gittoolbox.ui.statusbar.StatusBarManager;
import zielu.gittoolbox.util.ProjectAwares;

public class GitToolBoxProject extends AbstractProjectComponent {
  private final Logger log = Logger.getInstance(getClass());
  private PerRepoInfoCacheImpl perRepoInfoCache;
  private StatusBarManager statusBarManager;
  private ProjectViewManager projectViewManager;
  private ProjectAwares awares;

  public GitToolBoxProject(@NotNull Project project) {
    super(project);
  }

  @NotNull
  @SuppressFBWarnings({"NP_NULL_ON_SOME_PATH", "NP_NONNULL_RETURN_VIOLATION"})
  public static GitToolBoxProject getInstance(@NotNull Project project) {
    return project.getComponent(GitToolBoxProject.class);
  }

  @Override
  public void initComponent() {
    perRepoInfoCache = PerRepoInfoCacheImpl.create(myProject);
    statusBarManager = StatusBarManager.create(myProject);
    projectViewManager = ProjectViewManager.create(myProject);
    awares = ProjectAwares.create(
        perRepoInfoCache,
        statusBarManager,
        projectViewManager
    );
  }

  @Override
  public void disposeComponent() {
    perRepoInfoCache.dispose();
    statusBarManager.dispose();
    projectViewManager.dispose();
    awares.dispose();
  }

  public PerRepoInfoCache perRepoStatusCache() {
    return perRepoInfoCache;
  }

  @Override
  public void projectOpened() {
    awares.opened();
    log.debug("Project opened");
  }

  @Override
  public void projectClosed() {
    awares.closed();
    log.debug("Project closed");
  }
}

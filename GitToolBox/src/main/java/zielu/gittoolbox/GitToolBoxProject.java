package zielu.gittoolbox;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ui.projectview.ProjectViewManager;
import zielu.gittoolbox.ui.statusbar.StatusBarManager;
import zielu.gittoolbox.util.ProjectAwares;

public class GitToolBoxProject implements ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;
  private StatusBarManager statusBarManager;
  private ProjectViewManager projectViewManager;
  private ProjectAwares awares;

  public GitToolBoxProject(@NotNull Project project) {
    this.project = project;
  }

  @NotNull
  public static GitToolBoxProject getInstance(@NotNull Project project) {
    return project.getComponent(GitToolBoxProject.class);
  }

  @Override
  public void initComponent() {
    statusBarManager = StatusBarManager.create(project);
    projectViewManager = ProjectViewManager.create(project);
    awares = ProjectAwares.create(
        statusBarManager,
        projectViewManager
    );
  }

  @Override
  public void disposeComponent() {
    statusBarManager.dispose();
    statusBarManager = null;
    projectViewManager.dispose();
    projectViewManager = null;
    awares.dispose();
    awares = null;
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

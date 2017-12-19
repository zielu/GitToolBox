package zielu.gittoolbox.ui.projectview;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import git4idea.repo.GitRepository;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.ui.projectview.node.DecorableNode;
import zielu.gittoolbox.ui.projectview.node.DecorableNodeFactory;
import zielu.gittoolbox.util.GtUtil;
import zielu.gittoolbox.util.LogWatch;

public class ProjectViewDecorator implements ProjectViewNodeDecorator {
  private final Logger log = Logger.getInstance(getClass());

  private final NodeDecorationFactory decorationFactory = NodeDecorationFactory.getInstance();
  private final DecorableNodeFactory decorableNodeFactory = new DecorableNodeFactory();

  @Override
  public void decorate(ProjectViewNode projectViewNode, PresentationData presentation) {
    LogWatch decorateWatch = LogWatch.createStarted("Decorate");
    if (shouldDecorate(projectViewNode)) {
      final boolean debug = log.isDebugEnabled();
      LogWatch decorationCheckWatch = LogWatch.createStarted("Decoration check");
      DecorableNode decorableNode = decorableNodeFactory.nodeFor(projectViewNode);
      decorationCheckWatch.elapsed("NodeFor [", projectViewNode.getName(), "]").finish();
      if (decorableNode != null) {
        GitRepository repo = decorableNode.getRepo();
        if (repo != null) {
          applyDecoration(projectViewNode.getProject(), repo, projectViewNode, presentation);
          decorateWatch.elapsed("Decoration ", "[", projectViewNode.getName(), "]");
        } else {
          if (debug) {
            log.debug("No git repo: ", projectViewNode);
          }
        }
      } else {
        if (debug) {
          log.debug("Not decorable node: ", projectViewNode);
        }
      }
    }
    decorateWatch.finish();
  }

  @Override
  public void decorate(PackageDependenciesNode packageDependenciesNode,
                       ColoredTreeCellRenderer coloredTreeCellRenderer) {
    if (log.isDebugEnabled()) {
      log.debug("Decorate package dependencies");
    }
  }

  private boolean shouldDecorate(ProjectViewNode projectViewNode) {
    Project project = projectViewNode.getProject();
    return projectViewNode.getName() != null && project != null && GtUtil.isNotDumb(project)
        && GitToolBoxConfig.getInstance().showProjectViewStatus;
  }

  private void applyDecoration(Project project, GitRepository repo, ProjectViewNode projectViewNode,
                               PresentationData presentation) {
    final LogWatch decorateApplyWatch = LogWatch.createStarted("Decorate apply");
    PerRepoInfoCache cache = GitToolBoxProject.getInstance(project).perRepoStatusCache();
    NodeDecoration decoration = decorationFactory.decorationFor(repo, cache.getInfo(repo));
    boolean applied = decoration.apply(projectViewNode, presentation);
    decorateApplyWatch.elapsed("for ", repo).finish();
    presentation.setChanged(applied);
  }
}

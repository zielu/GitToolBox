package zielu.gittoolbox.ui.projectview;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import git4idea.repo.GitRepository;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.util.diagnostics.LogWatch;

public class ProjectViewDecorator implements ProjectViewNodeDecorator {
  private final Logger log = Logger.getInstance(getClass());

  private final NodeDecorationFactory decorationFactory = NodeDecorationFactory.getInstance();
  private final GitRepositoryFinder repoFinder = new GitRepositoryFinder();

  @Override
  public void decorate(ProjectViewNode node, PresentationData presentation) {
    LogWatch decorateWatch = LogWatch.createStarted("Decorate");
    if (shouldDecorate(node)) {
      LogWatch getRepoWatch = LogWatch.createStarted("Get repo [", node.getName(), "]");
      GitRepository repo = repoFinder.getRepoFor(node);
      getRepoWatch.finish();
      if (repo != null) {
        applyDecoration(node.getProject(), repo, node, presentation);
        decorateWatch.elapsed("Decoration ", "[", node.getName(), "]");
      } else {
        log.debug("No git repo: ", node);
      }
    }
    decorateWatch.finish();
  }

  @Override
  public void decorate(PackageDependenciesNode packageDependenciesNode,
                       ColoredTreeCellRenderer coloredTreeCellRenderer) {
    log.debug("Decorate package dependencies");
  }

  private boolean shouldDecorate(ProjectViewNode projectViewNode) {
    Project project = projectViewNode.getProject();
    return project != null && GitToolBoxConfig.getInstance().showProjectViewStatus;
  }

  private void applyDecoration(Project project, GitRepository repo, ProjectViewNode projectViewNode,
                               PresentationData presentation) {
    final LogWatch decorateApplyWatch = LogWatch.createStarted("Decorate apply");
    PerRepoInfoCache cache = PerRepoInfoCache.getInstance(project);
    NodeDecoration decoration = decorationFactory.decorationFor(repo, cache.getInfo(repo));
    boolean applied = decoration.apply(projectViewNode, presentation);
    decorateApplyWatch.elapsed("for ", repo).finish();
    presentation.setChanged(applied);
  }
}

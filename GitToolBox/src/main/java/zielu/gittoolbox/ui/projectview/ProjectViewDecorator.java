package zielu.gittoolbox.ui.projectview;

import com.codahale.metrics.Timer;
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
import zielu.gittoolbox.metrics.MetricsHost;

public class ProjectViewDecorator implements ProjectViewNodeDecorator {
  private final Logger log = Logger.getInstance(getClass());

  private final NodeDecorationFactory decorationFactory = NodeDecorationFactory.getInstance();
  private final GitRepositoryFinder repoFinder = new GitRepositoryFinder();

  @Override
  public void decorate(ProjectViewNode node, PresentationData presentation) {
    if (shouldDecorate(node)) {
      decorateLatency(node).time(() -> doDecorate(node, presentation));
    }
  }

  @Override
  public void decorate(PackageDependenciesNode packageDependenciesNode,
                       ColoredTreeCellRenderer coloredTreeCellRenderer) {
    log.debug("Decorate package dependencies");
  }

  private Timer decorateLatency(ProjectViewNode node) {
    return MetricsHost.project(node.getProject()).timer("decorate");
  }

  private Timer repoForLatency(ProjectViewNode node) {
    return MetricsHost.project(node.getProject()).timer("repo-for");
  }

  private Timer decorateApplyLatency(ProjectViewNode node) {
    return MetricsHost.project(node.getProject()).timer("decorate-apply");
  }

  private void doDecorate(ProjectViewNode node, PresentationData presentation) {
    GitRepository repo = repoForLatency(node).timeSupplier(() -> repoFinder.getRepoFor(node));
    if (repo != null) {
      decorateApplyLatency(node).time(() -> applyDecoration(node.getProject(), repo, node, presentation));
    } else {
      log.debug("No git repo: ", node);
    }
  }

  private boolean shouldDecorate(ProjectViewNode projectViewNode) {
    Project project = projectViewNode.getProject();
    return project != null && GitToolBoxConfig.getInstance().showProjectViewStatus;
  }

  private void applyDecoration(Project project, GitRepository repo, ProjectViewNode projectViewNode,
                               PresentationData presentation) {
    PerRepoInfoCache cache = PerRepoInfoCache.getInstance(project);
    NodeDecoration decoration = decorationFactory.decorationFor(repo, cache.getInfo(repo));
    boolean applied = decoration.apply(projectViewNode, presentation);
    presentation.setChanged(applied);
  }
}

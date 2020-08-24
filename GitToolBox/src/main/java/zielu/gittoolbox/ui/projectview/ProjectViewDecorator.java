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
import zielu.gittoolbox.config.AppConfig;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.ProjectMetrics;

public class ProjectViewDecorator implements ProjectViewNodeDecorator {
  private final Logger log = Logger.getInstance(getClass());

  private final NodeDecorationFactory decorationFactory = NodeDecorationFactory.getInstance();
  private final GitRepositoryFinder repoFinder = new GitRepositoryFinder();

  @Override
  public void decorate(ProjectViewNode node, PresentationData presentation) {
    if (shouldDecorate(node)) {
      Metrics metrics = ProjectMetrics.getInstance(node.getProject());
      metrics.timer("decorate").time(() -> doDecorate(node, presentation, metrics));
    }
  }

  @Override
  public void decorate(PackageDependenciesNode packageDependenciesNode,
                       ColoredTreeCellRenderer coloredTreeCellRenderer) {
    log.debug("Decorate package dependencies");
  }

  private void doDecorate(ProjectViewNode<?> node, PresentationData presentation, Metrics metrics) {
    Timer repoForLatency = metrics.timer("decorate-repo-for");
    GitRepository repo = repoForLatency.timeSupplier(() -> repoFinder.getRepoFor(node));
    if (repo != null) {
      Timer applyLatency = metrics.timer("decorate-apply");
      applyLatency.time(() -> applyDecoration(node.getProject(), repo, node, presentation));
    }
  }

  private boolean shouldDecorate(ProjectViewNode<?> projectViewNode) {
    Project project = projectViewNode.getProject();
    boolean result = project != null && AppConfig.get().getShowProjectViewStatus();
    if (!result && log.isDebugEnabled()) {
      log.debug("No project for node ", projectViewNode.getClass().getSimpleName(),
          " title=", projectViewNode.getTitle());
    }
    return result;
  }

  private void applyDecoration(Project project, GitRepository repo, ProjectViewNode<?> projectViewNode,
                               PresentationData presentation) {
    PerRepoInfoCache cache = PerRepoInfoCache.getInstance(project);
    NodeDecoration decoration = decorationFactory.decorationFor(repo, cache.getInfo(repo));
    boolean applied = decoration.apply(projectViewNode, presentation);
    presentation.setChanged(applied);
  }
}

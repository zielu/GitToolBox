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
import zielu.gittoolbox.GitToolBoxAppMetrics;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.config.GitToolBoxConfig;

public class ProjectViewDecorator implements ProjectViewNodeDecorator {
  private final Logger log = Logger.getInstance(getClass());

  private final NodeDecorationFactory decorationFactory = NodeDecorationFactory.getInstance();
  private final GitRepositoryFinder repoFinder = new GitRepositoryFinder();
  private final Timer decorateLatency;
  private final Timer repoForLatency;
  private final Timer decorateApplyLatency;

  public ProjectViewDecorator(GitToolBoxAppMetrics metrics) {
    decorateLatency = metrics.timer("decorate");
    repoForLatency = metrics.timer("repo_for");
    decorateApplyLatency = metrics.timer("decorate_apply");
  }

  @Override
  public void decorate(ProjectViewNode node, PresentationData presentation) {
    if (shouldDecorate(node)) {
      decorateLatency.time(() -> doDecorate(node, presentation));
    }
  }

  @Override
  public void decorate(PackageDependenciesNode packageDependenciesNode,
                       ColoredTreeCellRenderer coloredTreeCellRenderer) {
    log.debug("Decorate package dependencies");
  }

  private void doDecorate(ProjectViewNode node, PresentationData presentation) {
    GitRepository repo = repoForLatency.timeSupplier(() -> repoFinder.getRepoFor(node));
    if (repo != null) {
      decorateApplyLatency.time(() -> applyDecoration(node.getProject(), repo, node, presentation));
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

package zielu.gittoolbox.ui.projectView;

import com.google.common.base.Optional;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import git4idea.GitUtil;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.apache.commons.lang.StringUtils;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.Status;
import zielu.gittoolbox.ui.StatusPresenter;
import zielu.gittoolbox.util.LogWatch;

public class GitProjectViewDecorator implements ProjectViewNodeDecorator {
    private final Logger LOG = Logger.getInstance(getClass());
    private final LogWatch moduleCheckWatch = LogWatch.create(LOG, "Module check");
    private final LogWatch branchWatch = LogWatch.create(LOG, "Branch get");
    private final LogWatch decorateWatch = LogWatch.create(LOG, "Decorate");

    private boolean isModuleNode(ProjectViewNode node) {
        moduleCheckWatch.start();
        boolean isModule = node.getVirtualFile() != null && ProjectRootsUtil.isModuleContentRoot(node.getVirtualFile(), node.getProject());
        moduleCheckWatch.finish();
        return isModule;
    }

    @Override
    public void decorate(ProjectViewNode projectViewNode, PresentationData presentation) {
        decorateWatch.start();
        if (GitToolBoxConfig.getInstance().showProjectViewStatus) {
            if (isModuleNode(projectViewNode)) {
                GitRepositoryManager repoManager = GitUtil.getRepositoryManager(projectViewNode.getProject());
                GitRepository repo = repoManager.getRepositoryForFile(projectViewNode.getVirtualFile());
                decorateWatch.elapsed("Repo find");
                if (repo != null) {
                    PerRepoInfoCache cache = GitToolBoxProject.getInstance(projectViewNode.getProject()).perRepoStatusCache();
                    StringBuilder location = new StringBuilder();
                    String existingLocation = presentation.getLocationString();
                    if (StringUtils.isNotBlank(existingLocation)) {
                        location.append(existingLocation).append(" - ");
                    }
                    decorateWatch.elapsed("Location part 1");
                    branchWatch.start();
                    location.append(GitBranchUtil.getDisplayableBranchText(repo));
                    branchWatch.finish();
                    Optional<GitAheadBehindCount> countOptional = cache.getInfo(repo).count;
                    if (countOptional.isPresent()) {
                        GitAheadBehindCount count = countOptional.get();
                        if (count.status() == Status.Success) {
                            StatusPresenter presenter = GitToolBoxConfig.getInstance().getPresenter();
                            String text = presenter.nonZeroAheadBehindStatus(count.ahead.value(), count.behind.value());
                            if (StringUtils.isNotBlank(text)) {
                                location.append(" ");
                            }
                            location.append(text);
                        }
                        decorateWatch.elapsed("Location part 2");
                    }
                    presentation.setLocationString(location.toString());
                    presentation.setChanged(true);
                }
            }
        }
        decorateWatch.finish();
    }

    @Override
    public void decorate(PackageDependenciesNode packageDependenciesNode, ColoredTreeCellRenderer coloredTreeCellRenderer) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Decorate package dependencies");
        }
    }
}

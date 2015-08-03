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
import zielu.gittoolbox.cache.PerRepoStatusCache;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.Status;
import zielu.gittoolbox.ui.StatusPresenter;

public class GitProjectViewDecorator implements ProjectViewNodeDecorator {
    private final Logger LOG = Logger.getInstance(getClass());

    private boolean isModuleNode(ProjectViewNode node) {
        return node.getVirtualFile() != null && ProjectRootsUtil.isModuleContentRoot(node.getVirtualFile(), node.getProject());
    }

    @Override
    public void decorate(ProjectViewNode projectViewNode, PresentationData presentation) {
        if (isModuleNode(projectViewNode)) {
            GitRepositoryManager repoManager = GitUtil.getRepositoryManager(projectViewNode.getProject());
            GitRepository repo = repoManager.getRepositoryForFile(projectViewNode.getVirtualFile());
            if (repo != null) {
                PerRepoStatusCache cache = GitToolBoxProject.getInstance(projectViewNode.getProject()).perRepoStatusCache();
                StringBuilder location = new StringBuilder();
                String existingLocation = presentation.getLocationString();
                if (StringUtils.isNotBlank(existingLocation)) {
                    location.append(existingLocation).append(" - ");
                }
                location.append(GitBranchUtil.getDisplayableBranchText(repo));
                Optional<GitAheadBehindCount> countOptional = cache.get(repo);
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
                }
                presentation.setLocationString(location.toString());
                presentation.setChanged(true);
            }
        }
    }

    @Override
    public void decorate(PackageDependenciesNode packageDependenciesNode, ColoredTreeCellRenderer coloredTreeCellRenderer) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Decorate package dependencies");
        }
    }
}

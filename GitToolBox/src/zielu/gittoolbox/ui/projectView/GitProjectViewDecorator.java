package zielu.gittoolbox.ui.projectView;

import com.google.common.base.Optional;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import git4idea.GitUtil;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
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

    @Nullable
    private String getCountText(GitToolBoxConfig config, Optional<GitAheadBehindCount> countOptional) {
        if (countOptional.isPresent()) {
            GitAheadBehindCount count = countOptional.get();
            if (count.status() == Status.Success) {
                StatusPresenter presenter = config.getPresenter();
                String text = presenter.nonZeroAheadBehindStatus(count.ahead.value(), count.behind.value());
                if (StringUtils.isNotBlank(text)) {
                    return text;
                }
            }
        }
        return null;
    }

    private String makeStatusLocation(GitToolBoxConfig config,
                                  String existingLocation, GitRepository repo,
                                  Optional<GitAheadBehindCount> countOptional) {
        String locationPath = null;
        if (config.showProjectViewLocationPath && StringUtils.isNotBlank(existingLocation)) {
            locationPath = existingLocation;
        }
        branchWatch.start();
        String branch = GitBranchUtil.getDisplayableBranchText(repo);
        branchWatch.finish();
        String count = getCountText(config, countOptional);
        StringBuilder status = new StringBuilder(branch);
        if (count != null) {
            status.append(" ").append(count);
        }
        StringBuilder location = new StringBuilder();
        if (config.showProjectViewStatusBeforeLocation) {
            location.append(status.toString());
            if (locationPath != null) {
                location.append(" - ").append(locationPath);
            }
        } else {
            if (locationPath != null) {
                location.append(locationPath).append(" - ").append(status.toString());
            } else {
                location.append(status.toString());
            }
        }
        return location.toString();
    }

    @Override
    public void decorate(ProjectViewNode projectViewNode, PresentationData presentation) {
        decorateWatch.start();
        GitToolBoxConfig config = GitToolBoxConfig.getInstance();
        if (config.showProjectViewStatus) {
            if (isModuleNode(projectViewNode)) {
                Project project = projectViewNode.getProject();
                VirtualFile file = projectViewNode.getVirtualFile();
                if (project != null && file != null) {
                    GitRepositoryManager repoManager = GitUtil.getRepositoryManager(project);
                    GitRepository repo = repoManager.getRepositoryForFile(file);
                    decorateWatch.elapsed("Repo find");
                    if (repo != null) {
                        PerRepoInfoCache cache = GitToolBoxProject.getInstance(project).perRepoStatusCache();
                        String existingLocation = presentation.getLocationString();
                        Optional<GitAheadBehindCount> countOptional = cache.getInfo(repo).count;
                        String location = makeStatusLocation(config, existingLocation, repo, countOptional);
                        decorateWatch.elapsed("Location make status");
                        presentation.setLocationString(location);
                        presentation.setChanged(true);
                    }
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

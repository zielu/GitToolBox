package zielu.gittoolbox.ui.projectView;

import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.util.LogWatch;

public class ProjectViewDecorator implements ProjectViewNodeDecorator {
    private final Logger LOG = Logger.getInstance(getClass());
    private final LogWatch moduleCheckWatch = LogWatch.create(LOG, "Module check");
    private final LogWatch decorateWatch = LogWatch.create(LOG, "Decorate");
    private final LogWatch decorateApplyWatch = LogWatch.create(LOG, "Decorate apply");

    private final NodeDecorationFactory decorationFactory = NodeDecorationFactory.getInstance();

    private boolean isModuleNode(ProjectViewNode node) {
        moduleCheckWatch.start();
        VirtualFile file = node.getVirtualFile();
        Project project = node.getProject();
        boolean isModule = file != null && project != null
            && file.isDirectory() && ProjectRootsUtil.isModuleContentRoot(file, project);
        moduleCheckWatch.elapsed("[", node.getName(), "]").finish();
        return isModule;
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
                    GitRepository repo = getRepoForFile(project, file);
                    decorateWatch.elapsed("Repo find", file);
                    if (repo != null) {
                        applyDecoration(project, repo, projectViewNode, presentation);
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("No git repo for: " + file);
                        }
                    }
                }
            }
        }
        decorateWatch.finish();
    }

    @Nullable
    private GitRepository getRepoForFile(Project project, VirtualFile file) {
        VcsRepositoryManager repoManager = VcsRepositoryManager.getInstance(project);
        Repository repo = repoManager.getRepositoryForFile(file, true);
        if (repo != null && GitVcs.NAME.equals(repo.getVcs().getName())) {
            return (GitRepository) repo;
        }
        return null;
    }

    private void applyDecoration(Project project, GitRepository repo, ProjectViewNode projectViewNode, PresentationData presentation) {
        decorateApplyWatch.start();
        PerRepoInfoCache cache = GitToolBoxProject.getInstance(project).perRepoStatusCache();
        GitAheadBehindCount countOptional = cache.getInfo(repo).count;
        NodeDecoration decoration = decorationFactory.decorationFor(repo, countOptional);
        boolean applied = decoration.apply(projectViewNode, presentation);
        decorateApplyWatch.elapsed("for ", repo).finish();
        decorateWatch.elapsed("Decoration ", "[", projectViewNode.getName() + "]");
        //presentation.setChanged(applied);
    }

    @Override
    public void decorate(PackageDependenciesNode packageDependenciesNode, ColoredTreeCellRenderer coloredTreeCellRenderer) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Decorate package dependencies");
        }
    }
}

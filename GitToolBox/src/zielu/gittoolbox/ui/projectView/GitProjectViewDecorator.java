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
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.util.LogWatch;

public class GitProjectViewDecorator implements ProjectViewNodeDecorator {
    private final Logger LOG = Logger.getInstance(getClass());
    private final LogWatch moduleCheckWatch = LogWatch.create(LOG, "Module check");
    private final LogWatch decorateWatch = LogWatch.create(LOG, "Decorate");

    private final NodeDecorationFactory decorationFactory = NodeDecorationFactory.getInstance();

    private boolean isModuleNode(ProjectViewNode node) {
        moduleCheckWatch.start();
        boolean isModule = node.getVirtualFile() != null && node.getProject() != null
            && ProjectRootsUtil.isModuleContentRoot(node.getVirtualFile(), node.getProject());
        moduleCheckWatch.finish();
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
                    GitRepositoryManager repoManager = GitUtil.getRepositoryManager(project);
                    GitRepository repo = repoManager.getRepositoryForFile(file);
                    decorateWatch.elapsed("Repo find");
                    if (repo != null) {
                        PerRepoInfoCache cache = GitToolBoxProject.getInstance(project).perRepoStatusCache();
                        Optional<GitAheadBehindCount> countOptional = cache.getInfo(repo).count;
                        NodeDecoration decoration = decorationFactory.decorationFor(repo, countOptional);
                        boolean applied = decoration.apply(projectViewNode, presentation);
                        decorateWatch.elapsed("Decoration");
                        presentation.setChanged(applied);
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

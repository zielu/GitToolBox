package zielu.gittoolbox.ui.projectView.node;

import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.Nullable;

public class DecorableModuleNode extends AbstractDecorableNode {
    public DecorableModuleNode(ProjectViewNode node) {
        super(node);
    }

    @Override
    @Nullable
    public GitRepository getRepo() {
        Pair<Project, VirtualFile> projectAndFile = getProjectAndFile();
        if (projectAndFile != null) {
            return getRepoForRoot(projectAndFile);
        }
        return null;
    }

    @Nullable
    private GitRepository getRepoForRoot(Pair<Project, VirtualFile> projectAndFile) {
        VcsRepositoryManager repoManager = VcsRepositoryManager.getInstance(projectAndFile.getFirst());
        Repository repo = repoManager.getRepositoryForFile(projectAndFile.getSecond());
        return getGitRepo(repo);
    }
}

package zielu.gittoolbox.ui.projectView.node;

import com.intellij.dvcs.repo.Repository;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractDecorableNode implements DecorableNode {
    protected final ProjectViewNode node;

    protected AbstractDecorableNode(ProjectViewNode node) {
        this.node = node;
    }

    @Nullable
    protected GitRepository getGitRepo(Repository repo) {
        if (repo != null && GitVcs.NAME.equals(repo.getVcs().getName())) {
            return (GitRepository) repo;
        }
        return null;
    }

    @Nullable
    protected Pair<Project, VirtualFile> getProjectAndFile() {
        Project project = node.getProject();
        VirtualFile file = node.getVirtualFile();
        if (project != null && file != null) {
            return Pair.create(project, file);
        }
        return null;
    }
}

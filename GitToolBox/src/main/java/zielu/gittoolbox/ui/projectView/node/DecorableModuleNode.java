package zielu.gittoolbox.ui.projectView.node;

import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

public class DecorableModuleNode extends AbstractDecorableNode {
    public DecorableModuleNode(ProjectViewNode node) {
        super(node);
    }

    @Nullable
    protected Repository getRepoFor(VcsRepositoryManager repoManager, VirtualFile file) {
        return repoManager.getRepositoryForFile(file, true);
    }
}

package zielu.gittoolbox.ui.projectView.node;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class DecorableNodeFactory {
    public DecorableNode nodeFor(ProjectViewNode node) {
        if (isModuleNode(node)) {
            return new DecorableModuleNode(node);
        } else if (node instanceof PsiDirectoryNode) {
            return new DecorableDirectoryNode((PsiDirectoryNode) node);
        } else {
            return null;
        }
    }

    private boolean isModuleNode(ProjectViewNode node) {
        VirtualFile file = node.getVirtualFile();
        Project project = node.getProject();
        return file != null && project != null && file.isDirectory() && ProjectRootsUtil.isModuleContentRoot(file, project);
    }
}

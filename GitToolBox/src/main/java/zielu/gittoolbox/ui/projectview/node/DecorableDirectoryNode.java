package zielu.gittoolbox.ui.projectview.node;

import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.vfs.VirtualFile;

public class DecorableDirectoryNode extends AbstractDecorableNode {
  public DecorableDirectoryNode(PsiDirectoryNode node) {
    super(node);
  }

  @Override
  protected Repository getRepoFor(VcsRepositoryManager repoManager, VirtualFile file) {
    return repoManager.getRepositoryForRootQuick(file);
  }
}

package zielu.gittoolbox.ui.projectview;

import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import java.util.function.BiFunction;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.cache.VirtualFileRepoCache;

public class GitRepositoryFinder {
  private final Logger log = Logger.getInstance(getClass());

  @Nullable
  public GitRepository getRepoFor(ProjectViewNode node) {
    GitRepository repository = null;
    if (isModuleNode(node)) {
      repository = findForModule(node);
    } else if (node instanceof PsiDirectoryNode) {
      repository = findForDirectory(node);
    }
    log.debug("Repo for ", node, " is: ", repository);
    return repository;
  }

  private GitRepository findForModule(ProjectViewNode node) {
    return findRepo(node, VirtualFileRepoCache::getRepoForDir);
  }

  private GitRepository findForDirectory(ProjectViewNode node) {
    return findRepo(node, VirtualFileRepoCache::getRepoForRoot);
  }

  private GitRepository findRepo(ProjectViewNode node,
                                 BiFunction<VirtualFileRepoCache, VirtualFile, GitRepository> finder) {
    Project project = node.getProject();
    VirtualFile file = node.getVirtualFile();
    if (project != null && file != null) {
      VirtualFileRepoCache cache = VirtualFileRepoCache.getInstance(project);
      return finder.apply(cache, file);
    }
    return null;
  }

  private boolean isModuleNode(ProjectViewNode node) {
    VirtualFile file = node.getVirtualFile();
    Project project = node.getProject();
    return file != null && project != null && file.isDirectory() && ProjectRootsUtil.isModuleContentRoot(file, project);
  }
}

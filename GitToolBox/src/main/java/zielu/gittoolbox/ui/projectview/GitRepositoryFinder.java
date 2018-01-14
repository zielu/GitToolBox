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
    return findRepo(node, (manager, file) -> manager.getRepositoryForFile(file, true));
  }

  private GitRepository findForDirectory(ProjectViewNode node) {
    return findRepo(node, VcsRepositoryManager::getRepositoryForRootQuick);
  }

  private GitRepository findRepo(ProjectViewNode node,
                                 BiFunction<VcsRepositoryManager, VirtualFile, Repository> finder) {
    Project project = node.getProject();
    VirtualFile file = node.getVirtualFile();
    if (project != null && file != null) {
      VcsRepositoryManager repoManager = VcsRepositoryManager.getInstance(project);
      Repository repository = finder.apply(repoManager, file);
      return getGitRepo(repository);
    }
    return null;
  }

  @Nullable
  private GitRepository getGitRepo(Repository repo) {
    if (repo != null && GitVcs.NAME.equals(repo.getVcs().getName())) {
      return (GitRepository) repo;
    }
    return null;
  }

  private boolean isModuleNode(ProjectViewNode node) {
    VirtualFile file = node.getVirtualFile();
    Project project = node.getProject();
    return file != null && project != null && file.isDirectory() && ProjectRootsUtil.isModuleContentRoot(file, project);
  }
}

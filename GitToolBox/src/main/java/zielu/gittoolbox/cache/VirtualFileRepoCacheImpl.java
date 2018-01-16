package zielu.gittoolbox.cache;

import com.google.common.base.Preconditions;
import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VirtualFileRepoCacheImpl implements VirtualFileRepoCache, ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;

  public VirtualFileRepoCacheImpl(@NotNull Project project) {
    this.project = project;
  }

  @Nullable
  @Override
  public GitRepository getRepoForRoot(@NotNull VirtualFile root) {
    Preconditions.checkArgument(root.isDirectory(), "%s is not a dir", root);
    return findRepoForRoot(root);
  }

  @Nullable
  private GitRepository findRepoForRoot(@NotNull VirtualFile root) {
    VcsRepositoryManager manager = VcsRepositoryManager.getInstance(project);
    return getGitRepo(manager.getRepositoryForRootQuick(root));
  }

  @Nullable
  private GitRepository getGitRepo(Repository repo) {
    if (repo != null && GitVcs.NAME.equals(repo.getVcs().getName())) {
      return (GitRepository) repo;
    }
    return null;
  }
}

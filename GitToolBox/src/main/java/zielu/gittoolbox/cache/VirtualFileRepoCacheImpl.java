package zielu.gittoolbox.cache;

import com.google.common.base.Preconditions;
import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VirtualFileRepoCacheImpl implements VirtualFileRepoCache, ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final VirtualFileManager vfManager;
  private final Project project;
  private VirtualFileListener vfListener;

  public VirtualFileRepoCacheImpl(@NotNull VirtualFileManager vfManager, @NotNull Project project) {
    this.vfManager = vfManager;
    this.project = project;
  }

  @Override
  public void initComponent() {
    vfListener = new VirtualFileListener() {
      @Override
      public void beforeFileDeletion(@NotNull VirtualFileEvent event) {
        evict(event.getFile());
      }

      @Override
      public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {
        evict(event.getFile());
      }
    };
    vfManager.addVirtualFileListener(vfListener);
  }

  @Override
  public void disposeComponent() {
    vfManager.removeVirtualFileListener(vfListener);
    vfListener = null;
  }

  @Nullable
  @Override
  public GitRepository getRepoForRoot(@NotNull VirtualFile root) {
    Preconditions.checkArgument(root.isDirectory(),"%s is not a dir", root);
    VcsRepositoryManager manager = VcsRepositoryManager.getInstance(project);
    return getGitRepo(manager.getRepositoryForRootQuick(root));
  }

  @Nullable
  @Override
  public GitRepository getRepoForDir(@NotNull VirtualFile dir) {
    Preconditions.checkArgument(dir.isDirectory(),"%s is not a dir", dir);
    VcsRepositoryManager manager = VcsRepositoryManager.getInstance(project);
    return getGitRepo(manager.getRepositoryForFile(dir, true));
  }

  @Nullable
  private GitRepository getGitRepo(Repository repo) {
    if (repo != null && GitVcs.NAME.equals(repo.getVcs().getName())) {
      return (GitRepository) repo;
    }
    return null;
  }

  private void evict(@NotNull VirtualFile file) {
    log.debug("Evict ", file);
  }
}

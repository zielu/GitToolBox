package zielu.gittoolbox.util;

import com.intellij.dvcs.DvcsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.LocalFilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.vcs.log.Hash;
import com.intellij.vcs.log.impl.HashImpl;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.config.GitVcsSettings;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.CalledInAwt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GtUtil {
  private GtUtil() {
    throw new IllegalStateException();
  }

  public static String name(@NotNull GitRepository repository) {
    return DvcsUtil.getShortRepositoryName(repository);
  }

  @NotNull
  public static GitVcs vcs(@NotNull GitRepository repository) {
    return repository.getVcs();
  }

  public static Hash hash(String hash) {
    return HashImpl.build(hash);
  }

  public static boolean hasRemotes(@NotNull GitRepository repository) {
    return !repository.getRemotes().isEmpty();
  }

  public static List<GitRepository> sort(Collection<GitRepository> repositories) {
    return DvcsUtil.sortRepositories(new ArrayList<>(repositories));
  }

  @Nullable
  @CalledInAwt
  public static GitRepository getCurrentRepositoryQuick(@NotNull Project project) {
    GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(project);
    return DvcsUtil.guessCurrentRepositoryQuick(project, repositoryManager, GitVcsSettings.getInstance(project)
        .getRecentRootPath());
  }

  public static List<GitRepository> getRepositoriesForRoots(@NotNull Project project, Collection<String> roots) {
    GitRepositoryManager manager = GitRepositoryManager.getInstance(project);
    VirtualFileManager vfManager = VirtualFileManager.getInstance();
    return roots.stream()
        .map(vfManager::findFileByUrl)
        .filter(Objects::nonNull)
        .map(manager::getRepositoryForRoot)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  public static Optional<GitRepository> getRepositoryForRoot(@NotNull Project project, String root) {
    GitRepositoryManager manager = GitRepositoryManager.getInstance(project);
    VirtualFileManager vfManager = VirtualFileManager.getInstance();
    return Optional.ofNullable(root).map(vfManager::findFileByUrl).map(manager::getRepositoryForRoot);
  }

  @NotNull
  public static FilePath localFilePath(@NotNull VirtualFile file) {
    return new LocalFilePath(file.getPath(), file.isDirectory());
  }

  public static boolean hasGitVcs(@NotNull Project project) {
    ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
    return vcsManager.findVcsByName(GitVcs.NAME) != null;
  }
}

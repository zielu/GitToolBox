package zielu.gittoolbox.compat;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import git4idea.repo.GitRepository;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.GtUtil;

public final class GitCompatUtil {
  private GitCompatUtil() {
    throw new IllegalStateException();
  }

  @NotNull
  public static Collection<GitRepository> getRepositoriesForFiles(@NotNull Project project,
                                                                  @NotNull Collection<File> files) {
    List<GitRepoInfo> repositories = getRepos(project);
    return files.stream().map(f -> getRepoFor(repositories, f)).filter(Optional::isPresent).map(Optional::get)
        .map(GitRepoInfo::getRepo).collect(Collectors.toSet());
  }

  private static List<GitRepoInfo> getRepos(@NotNull Project project) {
    return GtUtil.getRepositories(project).stream().map(GitRepoInfo::new).collect(Collectors.toList());
  }

  private static Optional<GitRepoInfo> getRepoFor(List<GitRepoInfo> repositories, File file) {
    return repositories.stream().filter(r -> r.isFileInRepo(file)).findFirst();
  }

  private static class GitRepoInfo {
    private final GitRepository repo;
    private final File rootFile;

    private GitRepoInfo(GitRepository repo) {
      this.repo = repo;
      rootFile = VfsUtilCore.virtualToIoFile(this.repo.getRoot());
    }

    boolean isFileInRepo(File file) {
      return FileUtil.isAncestor(rootFile, file, false);
    }

    GitRepository getRepo() {
      return repo;
    }
  }
}

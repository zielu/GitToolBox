package zielu.gittoolbox.util;

import com.intellij.dvcs.DvcsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.LocalFilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
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
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.CalledInAwt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GtUtil {
  private GtUtil() {
    throw new IllegalStateException();
  }

  @NotNull
  public static String name(@NotNull GitRepository repository) {
    return DvcsUtil.getShortRepositoryName(repository);
  }

  @NotNull
  public static GitVcs vcs(@NotNull GitRepository repository) {
    return repository.getVcs();
  }

  @NotNull
  public static Hash hash(@NotNull String hash) {
    return HashImpl.build(hash);
  }

  public static boolean hasRemotes(@NotNull GitRepository repository) {
    return !repository.getRemotes().isEmpty();
  }

  @NotNull
  public static List<GitRepository> sort(@NotNull Collection<GitRepository> repositories) {
    return DvcsUtil.sortRepositories(new ArrayList<>(repositories));
  }

  @Nullable
  @CalledInAwt
  public static GitRepository getCurrentRepositoryQuick(@NotNull Project project) {
    GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(project);
    String recentRootPath = GitVcsSettings.getInstance(project).getRecentRootPath();
    return DvcsUtil.guessCurrentRepositoryQuick(project, repositoryManager, recentRootPath);
  }

  @NotNull
  public static List<GitRepository> getRepositoriesForRoots(@NotNull Project project,
                                                            @NotNull Collection<String> roots) {
    return roots.stream()
        .map(GtUtil::findFileByUrl)
        .filter(Objects::nonNull)
        .map(vFile -> getRepoForRoot(project, vFile))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Nullable
  private static VirtualFile findFileByUrl(String url) {
    return VirtualFileManager.getInstance().findFileByUrl(url);
  }

  @Nullable
  private static GitRepository getRepoForRoot(@NotNull Project project, @Nullable VirtualFile file) {
    return GitRepositoryManager.getInstance(project).getRepositoryForRoot(file);
  }

  public static Optional<GitRepository> getRepositoryForRoot(@NotNull Project project,
                                                             @Nullable String root) {
    return Optional.ofNullable(root)
               .map(GtUtil::findFileByUrl)
               .map(vFile -> getRepoForRoot(project, vFile));
  }

  @NotNull
  public static FilePath localFilePath(@NotNull VirtualFile file) {
    return new LocalFilePath(file.getPath(), file.isDirectory());
  }

  public static boolean hasGitVcs(@NotNull Project project) {
    ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
    return vcsManager.findVcsByName(GitVcs.NAME) != null;
  }

  @NotNull
  public static VcsRevisionNumber getCurrentRevision(@NotNull Project project, @NotNull VirtualFile file) {
    DiffProvider diffProvider = GitVcs.getInstance(project).getDiffProvider();
    VcsRevisionNumber currentRevision = diffProvider.getCurrentRevision(file);
    return ObjectUtils.defaultIfNull(currentRevision, VcsRevisionNumber.NULL);
  }
}

package zielu.gittoolbox.compat;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public enum GitCompatUtil {
    ;

    public static Collection<GitRepository> getRepositoriesForFiles(@NotNull Project project, @NotNull Collection<File> files) {
        List<GitRepoInfo> repositories = getRepos(project);
        return files.stream().map(f -> getRepoFor(repositories, f)).filter(Optional::isPresent).map(Optional::get).map(GitRepoInfo::getRepo).
            collect(Collectors.toSet());
    }

    private static List<GitRepoInfo> getRepos(@NotNull Project project) {
        return GitRepositoryManager.getInstance(project).getRepositories().stream().map(GitRepoInfo::new).collect(Collectors.toList());
    }

    private static Optional<GitRepoInfo> getRepoFor(List<GitRepoInfo> repositories, File file) {
        return repositories.stream().filter(r -> r.isFileInRepo(file)).findFirst();
    }

    private static class GitRepoInfo {
        private final GitRepository myRepo;
        private final File myRootFile;

        private GitRepoInfo(GitRepository repo) {
            myRepo = repo;
            myRootFile = VfsUtil.virtualToIoFile(myRepo.getRoot());
        }

        boolean isFileInRepo(File file) {
            return FileUtil.isAncestor(myRootFile, file, false);
        }

        GitRepository getRepo() {
            return myRepo;
        }
    }
}

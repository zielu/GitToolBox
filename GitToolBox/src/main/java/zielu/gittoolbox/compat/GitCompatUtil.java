package zielu.gittoolbox.compat;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public enum GitCompatUtil {
    ;

    public static Collection<GitRepository> getRepositoriesForFiles(@NotNull Project project, @NotNull List<VirtualFile> files) {
        List<GitRepoInfo> repositories = GitRepositoryManager.getInstance(project).getRepositories().stream().map(GitRepoInfo::new).
            collect(Collectors.toList());
        return files.stream().map(f -> getRepoFor(repositories, f)).filter(Optional::isPresent).map(Optional::get).
            map(GitRepoInfo::getRepo).collect(Collectors.toSet());
    }

    private static Optional<GitRepoInfo> getRepoFor(List<GitRepoInfo> repositories, VirtualFile file) {
        return repositories.stream().filter(r -> r.isFileInRepo(file)).findFirst();
    }

    private static class GitRepoInfo {
        private final GitRepository myRepo;

        private GitRepoInfo(GitRepository repo) {
            this.myRepo = repo;
        }

        boolean isFileInRepo(VirtualFile file) {
            return VfsUtil.isAncestor(myRepo.getRoot(),file,false);
        }

        GitRepository getRepo() {
            return myRepo;
        }
    }
}

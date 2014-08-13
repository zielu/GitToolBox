package zielu.gittoolbox.util;

import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

public enum GtUtil {
    ;

    public static String name(@NotNull GitRepository repository) {
        return repository.getGitDir().getParent().getName();
    }
}

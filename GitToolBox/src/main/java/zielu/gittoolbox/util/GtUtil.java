package zielu.gittoolbox.util;

import com.intellij.dvcs.DvcsUtil;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

public enum GtUtil {
    ;

    public static String name(@NotNull GitRepository repository) {
        return DvcsUtil.getShortRepositoryName(repository);
    }

    public static boolean hasRemotes(@NotNull GitRepository repository) {
        return !repository.getRemotes().isEmpty();
    }
}

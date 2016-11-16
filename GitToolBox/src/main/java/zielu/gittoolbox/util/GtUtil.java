package zielu.gittoolbox.util;

import com.intellij.dvcs.DvcsUtil;
import com.intellij.vcs.log.Hash;
import com.intellij.vcs.log.impl.HashImpl;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public enum GtUtil {
    ;

    public static String name(@NotNull GitRepository repository) {
        return DvcsUtil.getShortRepositoryName(repository);
    }

    public static GitVcs vcs(@NotNull GitRepository repository) {
        return (GitVcs) repository.getVcs();
    }

    public static GitLock lock(@NotNull GitRepository repository) {
        return new GitLock(vcs(repository));
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
}

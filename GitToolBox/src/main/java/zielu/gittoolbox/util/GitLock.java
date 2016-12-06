package zielu.gittoolbox.util;

import com.google.common.base.Preconditions;
import git4idea.GitVcs;
import org.jetbrains.annotations.NotNull;

public class GitLock {
    private final GitVcs myVcs;

    public GitLock(@NotNull GitVcs vcs) {
        myVcs = Preconditions.checkNotNull(vcs);
    }

    public void readLock() {
        //issue #28
        //myVcs.getCommandLock().readLock().lock();
    }

    public void readUnlock() {
        //issue #28
        //myVcs.getCommandLock().readLock().unlock();
    }
}

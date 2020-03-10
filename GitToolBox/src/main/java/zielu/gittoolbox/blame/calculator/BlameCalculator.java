package zielu.gittoolbox.blame.calculator;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionDataProvider;

public interface BlameCalculator {
  @Nullable
  RevisionDataProvider annotate(@NotNull GitRepository repository,
                                @NotNull VirtualFile file,
                                @NotNull VcsRevisionNumber revision);
}

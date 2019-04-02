package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface BlameLoader {
  @NotNull
  BlameAnnotation annotate(@NotNull VirtualFile file) throws VcsException;

  @Nullable
  VcsRevisionNumber getCurrentRevision(@NotNull GitRepository repository) throws VcsException;
}

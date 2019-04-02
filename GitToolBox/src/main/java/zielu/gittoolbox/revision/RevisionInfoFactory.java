package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

interface RevisionInfoFactory {
  @NotNull
  RevisionInfo forLine(@NotNull RevisionDataProvider provider, @NotNull VcsRevisionNumber lineRevision, int lineNumber);

  @NotNull
  RevisionInfo forFile(@NotNull VirtualFile file, @NotNull VcsFileRevision revision);
}

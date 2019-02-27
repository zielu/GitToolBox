package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public interface RevisionCache {
  @NotNull
  RevisionInfo getForLine(@NotNull FileAnnotation annotation, int lineNumber);

  RevisionInfo getForFile(@NotNull VirtualFile file, @NotNull VcsFileRevision revision);
}

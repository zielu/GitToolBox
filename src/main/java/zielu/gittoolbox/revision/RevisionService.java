package zielu.gittoolbox.revision;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.AppUtil;

public interface RevisionService {
  static RevisionService getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, RevisionService.class);
  }

  @NotNull
  RevisionInfo getForLine(@NotNull RevisionDataProvider provider, int lineNumber);

  @Nullable
  String getCommitMessage(@NotNull VirtualFile file, @NotNull RevisionInfo revisionInfo);
}

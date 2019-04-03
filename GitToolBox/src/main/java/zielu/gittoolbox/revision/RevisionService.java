package zielu.gittoolbox.revision;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.AppUtil;

public interface RevisionService {
  Topic<RevisionServiceListener> UPDATES = Topic.create("Revision updated",
      RevisionServiceListener.class);

  static RevisionService getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, RevisionService.class);
  }

  @NotNull
  RevisionInfo getForLine(@NotNull RevisionDataProvider provider, int lineNumber);

  RevisionInfo getForFile(@NotNull VirtualFile file, @NotNull VcsFileRevision revision);

  @Nullable
  String getCommitMessage(@NotNull RevisionInfo revisionInfo);
}

package zielu.gittoolbox.blame;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.util.AppUtil;

public interface BlameService {
  Topic<BlameListener> BLAME_UPDATE = Topic.create("RevisionInfo updates", BlameListener.class);

  @NotNull
  RevisionInfo getFileBlame(@NotNull VirtualFile file);

  @NotNull
  RevisionInfo getDocumentLineBlame(@NotNull Document document, @NotNull VirtualFile file, int editorLineNumber);

  void fileClosed(@NotNull VirtualFile file);

  void invalidate(@NotNull VirtualFile file);

  void blameUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation);

  @NotNull
  static BlameService getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, BlameService.class);
  }

  @NotNull
  static Optional<BlameService> getExistingInstance(@NotNull Project project) {
    return AppUtil.getExistingServiceInstance(project, BlameService.class);
  }
}

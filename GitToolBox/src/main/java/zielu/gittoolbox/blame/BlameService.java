package zielu.gittoolbox.blame;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface BlameService {
  Topic<BlameListener> BLAME_UPDATE = Topic.create("Blame updates", BlameListener.class);

  @NotNull
  Blame getFileBlame(@NotNull VirtualFile file);

  @NotNull
  Blame getDocumentLineBlame(@NotNull Document document, @NotNull VirtualFile file, int editorLineNumber);

  void fileClosed(@NotNull VirtualFile file);

  void invalidate(@NotNull VirtualFile file);

  void blameUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation);

  @NotNull
  static BlameService getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, BlameService.class);
  }

  @NotNull
  static Optional<BlameService> getExistingInstance(@NotNull Project project) {
    return Optional.ofNullable(ServiceManager.getServiceIfCreated(project, BlameService.class));
  }
}

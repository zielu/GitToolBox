package zielu.gittoolbox.blame;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlameService {
  Topic<BlameListener> BLAME_UPDATE = Topic.create("Blame updates", BlameListener.class);
  @Nullable
  Blame getFileBlame(@NotNull VirtualFile file);

  @Nullable
  Blame getCurrentLineBlame(@NotNull Editor editor, @NotNull VirtualFile file);

  void fileClosed(@NotNull VirtualFile file);

  static BlameService getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, BlameService.class);
  }
}

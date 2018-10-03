package zielu.gittoolbox.lens;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LensBlameService {
  @Nullable
  LensBlame getFileBlame(@NotNull VirtualFile file);

  @Nullable
  LensBlame getCurrentLineBlame(@NotNull Editor editor, @NotNull VirtualFile file);

  void fileClosed(@NotNull VirtualFile file);

  static LensBlameService getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, LensBlameService.class);
  }
}

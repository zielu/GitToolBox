package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface BlameEditorService {
  @Nullable
  Collection<LineExtensionInfo> getLineExtensions(@NotNull VirtualFile file, int editorLineNumber);

  @NotNull
  static BlameEditorService getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, BlameEditorService.class);
  }
}

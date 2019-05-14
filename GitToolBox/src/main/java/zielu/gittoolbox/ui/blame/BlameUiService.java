package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.util.AppUtil;

import java.util.Collection;
import java.util.Optional;

public interface BlameUiService {
  @Nullable
  Collection<LineExtensionInfo> getLineExtensions(@NotNull VirtualFile file, int editorLineIndex);

  @Nullable
  String getBlameStatus(@NotNull VirtualFile file, int editorLineIndex);

  void colorsSchemeChanged(@NotNull EditorColorsScheme colorsScheme);

  void configChanged(@NotNull GitToolBoxConfig2 previous, @NotNull GitToolBoxConfig2 current);

  void blameUpdated(@NotNull VirtualFile file);

  @NotNull
  static BlameUiService getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, BlameUiService.class);
  }

  @NotNull
  static Optional<BlameUiService> getExistingInstance(@NotNull Project project) {
    return AppUtil.getExistingServiceInstance(project, BlameUiService.class);
  }
}

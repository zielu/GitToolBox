package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.config.GitToolBoxConfig2;

public class BlameEditorLinePainter extends EditorLinePainter {
  @Nullable
  @Override
  public Collection<LineExtensionInfo> getLineExtensions(@NotNull Project project, @NotNull VirtualFile file,
                                                         int editorLineIndex) {
    if (shouldShow(project)) {
      //lines are 0-indexed
      return BlameEditorService.getInstance(project).getLineExtensions(file, editorLineIndex);
    }
    return null;
  }

  private boolean shouldShow(Project project) {
    if (GitToolBoxConfig2.getInstance().showEditorInlineBlame) {
      return !DumbService.isDumb(project);
    }
    return false;
  }
}

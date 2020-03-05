package zielu.gittoolbox.ui.blame

import com.intellij.openapi.editor.EditorLinePainter
import com.intellij.openapi.editor.LineExtensionInfo
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.extension.blame.InlineBlameAllowedExtension

internal class BlameEditorLinePainter : EditorLinePainter() {
  override fun getLineExtensions(
    project: Project,
    file: VirtualFile,
    editorLineIndex: Int
  ): Collection<LineExtensionInfo>? {
    return if (shouldShow(project)) {
      BlameUiService.getInstance(project).getLineExtensions(file, editorLineIndex)
    } else null
  }

  private fun shouldShow(project: Project): Boolean {
    return GitToolBoxConfig2.getInstance().showEditorInlineBlame &&
      !DumbService.isDumb(project) &&
      InlineBlameAllowedExtension.isBlameAllowed(project)
  }
}

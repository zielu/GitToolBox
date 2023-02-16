package zielu.gittoolbox.ui.blame

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorLinePainter
import com.intellij.openapi.editor.LineExtensionInfo
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.config.AppConfig
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
    return AppConfig.getConfig().showEditorInlineBlame &&
      isNotInDumbMode(project) &&
      isAllowedByExtension(project)
  }

  private fun isAllowedByExtension(project: Project): Boolean {
    val blameAllowed = InlineBlameAllowedExtension.isBlameAllowed(project)
    if (log.isDebugEnabled && !blameAllowed) {
      log.debug("Inline blame blocked by allow extension")
    }
    return blameAllowed
  }

  private fun isNotInDumbMode(project: Project): Boolean {
    val dumb = DumbService.isDumb(project)
    if (log.isDebugEnabled && dumb) {
      log.debug("Inline blame blocked by dumb mode")
    }
    return !dumb
  }

  private companion object {
    private val log = Logger.getInstance(BlameEditorLinePainter::class.java)
  }
}

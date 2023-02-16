package zielu.gittoolbox.ui.statusbar

import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.blame.BlameService
import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.revision.RevisionInfo
import zielu.gittoolbox.ui.blame.BlameUiService

internal class BlameStatusWidgetFacade(private val project: Project) {

  fun getBlameStatus(file: VirtualFile, lineIndex: Int): String? {
    return BlameUiService.getInstance(project).getBlameStatus(file, lineIndex)
  }

  fun getBlameStatusTooltip(file: VirtualFile, lineIndex: Int): String? {
    return BlameUiService.getInstance(project).getBlameStatusTooltip(file, lineIndex)
  }

  fun getRevisionInfo(document: Document, file: VirtualFile, lineIndex: Int): RevisionInfo {
    return BlameService.getInstance(project).getDocumentLineIndexBlame(document, file, lineIndex)
  }

  fun getIsVisibleConfig(): Boolean {
    return AppConfig.getConfig().showBlameWidget
  }
}

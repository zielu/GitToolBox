package zielu.gittoolbox.ui.blame

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.DocumentEx
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.revision.RevisionInfo

internal object BlameUi {
  private const val noLine = Int.MAX_VALUE

  @JvmStatic
  fun isValidLineIndex(lineIndex: Int): Boolean {
    return lineIndex != noLine
  }

  @JvmStatic
  fun getCurrentLineIndex(editor: Editor?): Int {
    return editor?.run {
      val caretModel = this.caretModel
      return if (caretModel.isUpToDate) {
        val position = caretModel.logicalPosition
        position.line
      } else {
        noLine
      }
    } ?: noLine
  }

  @JvmStatic
  fun isDocumentInBulkUpdate(document: Document?): Boolean {
    return if (document is DocumentEx) {
      document.isInBulkUpdate
    } else false
  }

  @JvmStatic
  fun showBlamePopup(editor: Editor, file: VirtualFile, revisionInfo: RevisionInfo) {
    editor.project?.apply {
      BlamePopup(this, file, revisionInfo).showFor(editor.component)
    }
  }
}

package zielu.gittoolbox.ui.blame

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile

internal data class LineInfo(
  val file: VirtualFile,
  val editor: Editor,
  val document: Document,
  val index: Int,
  val generation: Int,
  val modified: Boolean
) {
  constructor(
    file: VirtualFile,
    editor: Editor,
    document: Document,
    index: Int,
    generation: Int
  ) : this(file, editor, document, index, generation, document.isLineModified(index))
}

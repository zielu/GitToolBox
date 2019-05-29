package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

class LineInfo {
  final VirtualFile file;
  final Editor editor;
  final Document document;
  final int lineIndex;
  final boolean lineModified;
  final int generation;

  LineInfo(@NotNull VirtualFile file, @NotNull Editor editor, @NotNull Document document,
           int lineIndex, int generation) {
    this.file = file;
    this.editor = editor;
    this.document = document;
    this.lineIndex = lineIndex;
    this.generation = generation;
    lineModified = document.isLineModified(lineIndex);
  }
}

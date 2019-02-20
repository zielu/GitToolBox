package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.util.Key;
import java.util.Collection;
import org.jetbrains.annotations.Nullable;

class BlameEditorData {
  static final Key<BlameEditorData> KEY = new Key<>("GitToolBox-blame-editor");

  private final int editorLine;
  private final Collection<LineExtensionInfo> lineInfo;

  BlameEditorData(int editorLine, @Nullable Collection<LineExtensionInfo> lineInfo) {
    this.editorLine = editorLine;
    this.lineInfo = lineInfo;
  }

  boolean isSameEditorLine(int editorLine) {
    return this.editorLine == editorLine;
  }

  @Nullable
  Collection<LineExtensionInfo> getLineInfo() {
    return lineInfo;
  }
}

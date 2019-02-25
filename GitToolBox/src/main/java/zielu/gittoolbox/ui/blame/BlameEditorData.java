package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.util.Key;
import java.util.Collection;
import org.jetbrains.annotations.Nullable;

class BlameEditorData {
  static final Key<BlameEditorData> KEY = new Key<>("GitToolBox-blame-editor");

  private final int editorLine;
  private final Collection<LineExtensionInfo> lineInfo;
  private final boolean lineModified;
  private final int generation;

  BlameEditorData(int editorLine, boolean lineModified, int generation,
                  @Nullable Collection<LineExtensionInfo> lineInfo) {
    this.editorLine = editorLine;
    this.lineInfo = lineInfo;
    this.lineModified = lineModified;
    this.generation = generation;
  }

  boolean isSameEditorLine(int editorLine) {
    return this.editorLine == editorLine;
  }

  boolean isSameGeneration(int generation) {
    return this.generation == generation;
  }

  boolean isLineModified() {
    return lineModified;
  }

  @Nullable
  Collection<LineExtensionInfo> getLineInfo() {
    return lineInfo;
  }
}

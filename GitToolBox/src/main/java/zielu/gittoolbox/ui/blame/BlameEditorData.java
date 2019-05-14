package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.revision.RevisionInfo;

class BlameEditorData {
  static final Key<BlameEditorData> KEY = new Key<>("GitToolBox-blame-editor");

  private final int editorLineIndex;
  private final boolean lineModified;
  private final int generation;
  private final RevisionInfo revisionInfo;

  BlameEditorData(int editorLineIndex, boolean lineModified, int generation, @NotNull RevisionInfo revisionInfo) {
    this.editorLineIndex = editorLineIndex;
    this.lineModified = lineModified;
    this.generation = generation;
    this.revisionInfo = revisionInfo;
  }

  boolean isSameEditorLineIndex(int editorLine) {
    return this.editorLineIndex == editorLine;
  }

  boolean isSameGeneration(int generation) {
    return this.generation == generation;
  }

  boolean isLineModified() {
    return lineModified;
  }

  @NotNull
  RevisionInfo getRevisionInfo() {
    return revisionInfo;
  }
}

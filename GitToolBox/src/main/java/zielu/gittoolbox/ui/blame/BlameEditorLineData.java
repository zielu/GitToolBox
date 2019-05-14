package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.util.Key;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionInfo;

class BlameEditorLineData {
  static final Key<BlameEditorLineData> KEY = new Key<>("GitToolBox-blame-editor-line-info");

  private final List<LineExtensionInfo> lineInfo;
  private final RevisionInfo revisionInfo;

  BlameEditorLineData(@Nullable List<LineExtensionInfo> lineInfo, @NotNull RevisionInfo revisionInfo) {
    this.lineInfo = lineInfo;
    this.revisionInfo = revisionInfo;
  }

  @Nullable
  List<LineExtensionInfo> getLineInfo() {
    return lineInfo;
  }

  boolean isSameRevision(RevisionInfo revisionInfo) {
    return Objects.equals(this.revisionInfo, revisionInfo);
  }
}

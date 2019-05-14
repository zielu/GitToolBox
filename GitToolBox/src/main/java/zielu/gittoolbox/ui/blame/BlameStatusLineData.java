package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.util.Key;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionInfo;

class BlameStatusLineData {
  static final Key<BlameStatusLineData> KEY = new Key<>("GitToolBox-blame-status-line-info");

  private final String lineInfo;
  private final RevisionInfo revisionInfo;

  BlameStatusLineData(@Nullable String lineInfo, @NotNull RevisionInfo revisionInfo) {
    this.lineInfo = lineInfo;
    this.revisionInfo = revisionInfo;
  }

  @Nullable
  String getLineInfo() {
    return lineInfo;
  }

  boolean isSameRevision(RevisionInfo revisionInfo) {
    return Objects.equals(this.revisionInfo, revisionInfo);
  }
}

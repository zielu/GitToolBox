package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.TIntObjectHashMap;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BlameAnnotationImpl implements BlameAnnotation {
  private final Map<VcsRevisionNumber, Blame> blames = new HashMap<>();
  private final TIntObjectHashMap<Blame> lineBlames = new TIntObjectHashMap<>();
  private final FileAnnotation annotation;

  BlameAnnotationImpl(@NotNull FileAnnotation annotation) {
    this.annotation = annotation;
  }

  @Nullable
  @Override
  public Blame getBlame(int lineNumber) {
    VcsRevisionNumber lineRevision = annotation.getLineRevisionNumber(lineNumber);
    if (lineRevision != null) {
      Blame blame = blames.computeIfAbsent(lineRevision, lineRev -> LineBlame.create(annotation, lineNumber));
      lineBlames.put(lineNumber, blame);
      return blame;
    }
    return null;
  }

  @Override
  public boolean isChanged(@NotNull VcsRevisionNumber revision) {
    return annotation.isBaseRevisionChanged(revision);
  }

  @Nullable
  @Override
  public VirtualFile getVirtualFile() {
    return annotation.getFile();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("file", getVirtualFile())
        .append("currentRevision", annotation.getCurrentRevision())
        .toString();
  }
}

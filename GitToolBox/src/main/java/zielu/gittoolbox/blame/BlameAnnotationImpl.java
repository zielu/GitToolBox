package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import gnu.trove.TIntObjectHashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.revision.RevisionService;

class BlameAnnotationImpl implements BlameAnnotation {
  private final FileAnnotation annotation;
  private final RevisionService revisionService;
  private final Map<VcsRevisionNumber, RevisionInfo> revisions;
  private final TIntObjectHashMap<VcsRevisionNumber> lineRevisions;

  BlameAnnotationImpl(@NotNull FileAnnotation annotation, @NotNull RevisionService revisionService) {
    this.annotation = annotation;
    this.revisionService = revisionService;
    lineRevisions =  new TIntObjectHashMap<>(this.annotation.getLineCount() + 1);
    revisions = new THashMap<>(this.annotation.getLineCount() + 1);
  }

  @NotNull
  @Override
  public RevisionInfo getBlame(int lineNumber) {
    VcsRevisionNumber revisionNumber = getLineRevisionNumber(lineNumber);
    if (revisionNumber != VcsRevisionNumber.NULL) {
      RevisionInfo revisionInfo = revisions.get(revisionNumber);
      if (revisionInfo == null) {
        revisionInfo = loadRevision(lineNumber);
      }
      return revisionInfo;
    }
    return RevisionInfo.EMPTY;
  }

  @NotNull
  private VcsRevisionNumber getLineRevisionNumber(int lineNumber) {
    VcsRevisionNumber revisionNumber = lineRevisions.get(lineNumber);
    if (revisionNumber == null) {
      VcsRevisionNumber lineRevisionNumber = annotation.getLineRevisionNumber(lineNumber);
      if (lineRevisionNumber == null) {
        lineRevisionNumber = VcsRevisionNumber.NULL;
      }
      lineRevisions.put(lineNumber, lineRevisionNumber);
      revisionNumber = lineRevisionNumber;
    }
    return revisionNumber;
  }

  @NotNull
  private RevisionInfo loadRevision(int lineNumber) {
    RevisionInfo revisionInfo = revisionService.getForLine(annotation, lineNumber);
    revisions.put(revisionInfo.getRevisionNumber(), revisionInfo);
    return revisionInfo;
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
  public boolean updateRevision(@NotNull RevisionInfo revisionInfo) {
    if (revisions.containsKey(revisionInfo.getRevisionNumber())) {
      revisions.put(revisionInfo.getRevisionNumber(), revisionInfo);
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("file", getVirtualFile())
        .append("currentRevision", annotation.getCurrentRevision())
        .toString();
  }
}

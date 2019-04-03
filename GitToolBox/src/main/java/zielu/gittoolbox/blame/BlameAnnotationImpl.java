package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionDataProvider;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.revision.RevisionService;

class BlameAnnotationImpl implements BlameAnnotation {
  private final RevisionDataProvider provider;
  private final RevisionService revisionService;
  private final Map<VcsRevisionNumber, RevisionInfo> revisions;
  private final VcsRevisionNumber[] lineRevisions;

  BlameAnnotationImpl(@NotNull RevisionDataProvider provider, @NotNull RevisionService revisionService) {
    this.provider = provider;
    this.revisionService = revisionService;
    lineRevisions = new VcsRevisionNumber[provider.getLineCount()];
    revisions = new THashMap<>(provider.getLineCount());
  }

  @NotNull
  @Override
  public RevisionInfo getBlame(int lineIndex) {
    VcsRevisionNumber revisionNumber = getLineRevisionNumber(lineIndex);
    if (VcsRevisionNumber.NULL.equals(revisionNumber)) {
      return RevisionInfo.EMPTY;
    } else {
      RevisionInfo revisionInfo = revisions.get(revisionNumber);
      if (revisionInfo == null) {
        revisionInfo = loadRevision(lineIndex);
      }
      return revisionInfo;
    }
  }

  @NotNull
  private VcsRevisionNumber getLineRevisionNumber(int lineIndex) {
    if (lineIndex < 0 || lineIndex >= lineRevisions.length) {
      return VcsRevisionNumber.NULL;
    }
    VcsRevisionNumber revisionNumber = lineRevisions[lineIndex];
    if (revisionNumber == null) {
      VcsRevisionNumber lineRevisionNumber = provider.getRevisionNumber(lineIndex);
      if (lineRevisionNumber == null) {
        lineRevisionNumber = VcsRevisionNumber.NULL;
      }
      lineRevisions[lineIndex] = lineRevisionNumber;
      revisionNumber = lineRevisionNumber;
    }
    return revisionNumber;
  }

  @NotNull
  private RevisionInfo loadRevision(int lineNumber) {
    RevisionInfo revisionInfo = revisionService.getForLine(provider, lineNumber);
    revisions.put(revisionInfo.getRevisionNumber(), revisionInfo);
    return revisionInfo;
  }

  @Override
  public boolean isChanged(@NotNull VcsRevisionNumber revision) {
    return !Objects.equals(provider.getCurrentRevisionNumber(), revision);
  }

  @Nullable
  @Override
  public VirtualFile getVirtualFile() {
    return provider.getFile();
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
        .append("currentRevision", provider.getCurrentRevisionNumber())
        .toString();
  }
}

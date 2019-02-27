package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.TIntObjectHashMap;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.revision.RevisionCache;

class BlameAnnotationImpl implements BlameAnnotation {
  private final FileAnnotation annotation;
  private final RevisionCache revisionCache;
  private final TIntObjectHashMap<RevisionInfo> lineBlames;

  BlameAnnotationImpl(@NotNull FileAnnotation annotation, @NotNull RevisionCache revisionCache) {
    this.annotation = annotation;
    this.revisionCache = revisionCache;
    lineBlames =  new TIntObjectHashMap<>(this.annotation.getLineCount());
  }

  @NotNull
  @Override
  public RevisionInfo getBlame(int lineNumber) {
    RevisionInfo revisionInfo = lineBlames.get(lineNumber);
    if (revisionInfo == null) {
      revisionInfo = loadBlame(lineNumber);
    }
    return revisionInfo;
  }

  @NotNull
  private RevisionInfo loadBlame(int lineNumber) {
    RevisionInfo revisionInfo = revisionCache.getForLine(annotation, lineNumber);
    lineBlames.put(lineNumber, revisionInfo);
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
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("file", getVirtualFile())
        .append("currentRevision", annotation.getCurrentRevision())
        .toString();
  }
}

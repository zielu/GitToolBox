package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.TIntObjectHashMap;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BlameAnnotationImpl implements BlameAnnotation {
  private final FileAnnotation annotation;
  private final BlameRevisionCache revisionCache;
  private final TIntObjectHashMap<Blame> lineBlames;

  BlameAnnotationImpl(@NotNull FileAnnotation annotation, @NotNull BlameRevisionCache revisionCache) {
    this.annotation = annotation;
    this.revisionCache = revisionCache;
    lineBlames =  new TIntObjectHashMap<>(this.annotation.getLineCount());
  }

  @NotNull
  @Override
  public Blame getBlame(int lineNumber) {
    Blame blame = lineBlames.get(lineNumber);
    if (blame == null) {
      blame = loadBlame(lineNumber);
    }
    return blame;
  }

  @NotNull
  private Blame loadBlame(int lineNumber) {
    Blame blame = revisionCache.getForLine(annotation, lineNumber);
    lineBlames.put(lineNumber, blame);
    return blame;
  }

  @Override
  public void invalidateBlames() {
    lineBlames.clear();
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

package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.annotate.FileAnnotation;
import org.jetbrains.annotations.NotNull;

interface BlameRevisionCache {
  @NotNull
  Blame getForLine(@NotNull FileAnnotation annotation, int lineNumber);
}

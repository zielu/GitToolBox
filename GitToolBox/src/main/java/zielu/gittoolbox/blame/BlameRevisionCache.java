package zielu.gittoolbox.blame;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.AppUtil;

public interface BlameRevisionCache {
  @NotNull
  Blame getForLine(@NotNull FileAnnotation annotation, int lineNumber);

  void invalidateAll();

  @NotNull
  static Optional<BlameRevisionCache> getExistingInstance(@NotNull Project project) {
    return AppUtil.getExistingServiceInstance(project, BlameRevisionCache.class);
  }
}

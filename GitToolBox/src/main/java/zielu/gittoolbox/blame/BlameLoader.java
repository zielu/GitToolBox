package zielu.gittoolbox.blame;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.AppUtil;

interface BlameLoader {

  @NotNull
  static BlameLoader getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, BlameLoader.class);
  }

  @NotNull
  static Optional<BlameLoader> getExistingInstance(@NotNull Project project) {
    return AppUtil.getExistingServiceInstance(project, BlameLoader.class);
  }

  @NotNull
  BlameAnnotation annotate(@NotNull VirtualFile file) throws VcsException;

  @NotNull
  VcsRevisionNumber getCurrentRevision(@NotNull GitRepository repository);

  void invalidateForRoot(@NotNull VirtualFile root);
}

package zielu.gittoolbox.blame;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.AppUtil;

interface BlameLoader {

  @NotNull
  static BlameLoader getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, BlameLoader.class);
  }

  @NotNull
  BlameAnnotation annotate(@NotNull VirtualFile file) throws VcsException;

  @Nullable
  VcsRevisionNumber getCurrentRevision(@NotNull GitRepository repository) throws VcsException;
}

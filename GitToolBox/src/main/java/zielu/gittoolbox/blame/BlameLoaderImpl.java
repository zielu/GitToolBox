package zielu.gittoolbox.blame;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BlameLoaderImpl implements BlameLoader {
  private final GitVcs git;

  BlameLoaderImpl(@NotNull Project project) {
    git = GitVcs.getInstance(project);
  }

  @NotNull
  @Override
  public FileAnnotation annotate(@NotNull VirtualFile file) throws VcsException {
    return git.getAnnotationProvider().annotate(file);
  }

  @Nullable
  @Override
  public VcsRevisionNumber getCurrentRevision(@NotNull GitRepository repository) throws VcsException {
    return git.parseRevisionNumber(repository.getCurrentRevision());
  }
}

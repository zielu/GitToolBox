package zielu.gittoolbox.blame;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.blame.calculator.BlameCalculator;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.revision.RevisionDataProvider;
import zielu.gittoolbox.revision.RevisionService;

class BlameLoaderImpl implements BlameLoader {
  private final Project project;
  private final RevisionService revisionService;
  private final GitVcs git;

  BlameLoaderImpl(@NotNull Project project, @NotNull RevisionService revisionService) {
    this.project = project;
    this.revisionService = revisionService;
    git = GitVcs.getInstance(project);
  }

  @NotNull
  @Override
  public BlameAnnotation annotate(@NotNull VirtualFile file) throws VcsException {
    try {
      BlameUtil.annotationLock(project, file);
      return incrementalAnnotation(file);
    } finally {
      BlameUtil.annotationUnlock(project, file);
    }
  }

  private BlameAnnotation incrementalAnnotation(@NotNull VirtualFile file) {
    BlameCalculator calculator = new BlameCalculator();
    GitRepository repo = VirtualFileRepoCache.getInstance(project).getRepoForFile(file);
    if (repo != null) {
      RevisionDataProvider provider = calculator.annotate(repo, file);
      if (provider != null) {
        return new BlameAnnotationImpl(provider, revisionService);
      }
    }
    return BlameAnnotation.EMPTY;
  }

  @Nullable
  @Override
  public VcsRevisionNumber getCurrentRevision(@NotNull GitRepository repository) throws VcsException {
    return git.parseRevisionNumber(repository.getCurrentRevision());
  }
}

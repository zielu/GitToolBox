package zielu.gittoolbox.blame;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.blame.calculator.CachingBlameCalculator;
import zielu.gittoolbox.revision.RevisionDataProvider;
import zielu.intellij.util.ZDisposeGuard;

class BlameLoaderImpl implements BlameLoader, Disposable {
  private final ZDisposeGuard disposeGuard = new ZDisposeGuard();
  private final BlameLoaderLocalGateway gateway;
  private final CachingBlameCalculator calculator;

  BlameLoaderImpl(@NotNull Project project) {
    gateway = new BlameLoaderLocalGateway(project);
    calculator = new CachingBlameCalculator(project);
    gateway.registerDisposable(this, calculator);
    gateway.registerDisposable(this, disposeGuard);
  }

  @NotNull
  @Override
  public BlameAnnotation annotate(@NotNull VirtualFile file) {
    if (disposeGuard.isActive()) {
      GitRepository repo = gateway.getRepoForFile(file);
      if (disposeGuard.isActive() && repo != null) {
        VcsRevisionNumber fileRevision = gateway.getCurrentRevisionNumber(file);
        RevisionDataProvider provider = calculator.annotate(repo, file, fileRevision);
        if (disposeGuard.isActive() && provider != null) {
          return new BlameAnnotationImpl(provider, gateway.getRevisionService());
        }
      }
    }
    return BlameAnnotation.EMPTY;
  }

  @NotNull
  @Override
  public VcsRevisionNumber getCurrentRevision(@NotNull GitRepository repository) {
    if (disposeGuard.isActive()) {
      return gateway.getCurrentRevisionNumber(repository);
    } else {
      return VcsRevisionNumber.NULL;
    }
  }

  @Override
  public void invalidateForRoot(@NotNull VirtualFile root) {
    if (disposeGuard.isActive()) {
      calculator.invalidateForRoot(root);
    }
  }

  @Override
  public void dispose() {
    //do nothing
  }
}

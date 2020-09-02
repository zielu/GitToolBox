package zielu.gittoolbox.blame;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.blame.calculator.BlameCalculator;
import zielu.gittoolbox.blame.calculator.CachingBlameCalculator;
import zielu.gittoolbox.revision.RevisionDataProvider;

class BlameLoaderImpl implements BlameLoader {
  private final BlameLoaderLocalGateway gateway;
  private final BlameCalculator calculator;

  BlameLoaderImpl(@NotNull Project project) {
    gateway = new BlameLoaderLocalGateway(project);
    calculator = new CachingBlameCalculator(project);
  }

  @NotNull
  @Override
  public BlameAnnotation annotate(@NotNull VirtualFile file) {
    GitRepository repo = gateway.getRepoForFile(file);
    if (repo != null) {
      VcsRevisionNumber fileRevision = gateway.getCurrentRevisionNumber(file);
      RevisionDataProvider provider = calculator.annotate(repo, file, fileRevision);
      if (provider != null) {
        return new BlameAnnotationImpl(provider, gateway.getRevisionService());
      }
    }
    return BlameAnnotation.EMPTY;
  }

  @NotNull
  @Override
  public VcsRevisionNumber getCurrentRevision(@NotNull GitRepository repository) {
    return gateway.getCurrentRevisionNumber(repository);
  }

  @Override
  public void invalidateForRoot(@NotNull VirtualFile root) {
    calculator.invalidateForRoot(root);
  }
}

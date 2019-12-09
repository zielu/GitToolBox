package zielu.gittoolbox.blame;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.blame.calculator.BlameCalculator;
import zielu.gittoolbox.revision.RevisionDataProvider;

class BlameLoaderImpl implements BlameLoader {
  private final BlameLoaderLocalGateway gateway;
  private final BlameCalculator calculator;

  BlameLoaderImpl(@NotNull Project project) {
    gateway = new BlameLoaderLocalGateway(project);
    calculator = new BlameCalculator(project);
  }

  @NotNull
  @Override
  public BlameAnnotation annotate(@NotNull VirtualFile file) {
    return incrementalAnnotation(file);
  }

  private BlameAnnotation incrementalAnnotation(@NotNull VirtualFile file) {
    GitRepository repo = gateway.getRepoForFile(file);
    if (repo != null) {
      RevisionDataProvider provider = calculator.annotate(repo, file);
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
}

package zielu.gittoolbox.fetch;

import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import git4idea.update.GitFetchResult;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.repo.GtConfig;
import zielu.gittoolbox.repo.GtRepositoryManager;

public class GtSvnFetcher {
  private final Project project;

  public GtSvnFetcher(Project project) {
    this.project = project;
  }

  public GitFetchResult fetch(@NotNull GitRepository repository) {
    GtRepositoryManager manager = GtRepositoryManager.getInstance(project);
    Optional<GtConfig> config = manager.configFor(repository);

    return GitFetchResult.success();
  }
}

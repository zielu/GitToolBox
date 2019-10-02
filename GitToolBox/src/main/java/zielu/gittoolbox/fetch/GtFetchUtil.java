package zielu.gittoolbox.fetch;

import com.intellij.openapi.project.Project;
import git4idea.fetch.GitFetchResult;
import git4idea.fetch.GitFetchSupport;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;

public final class GtFetchUtil {

  private GtFetchUtil() {
    throw new IllegalStateException();
  }

  /**
   * Fetch for single repository.
   * Taken from {@link git4idea.actions.GitFetch}
   *
   * @param repository repository to fetch
   */
  public static GitFetchResult fetch(@NotNull GitRepository repository) {
    Project project = repository.getProject();
    GitFetchSupport fetchSupport = GitFetchSupport.fetchSupport(project);
    return fetchSupport.fetchAllRemotes(Collections.singleton(repository));
  }

  public static GitFetchResult fetch(@NotNull Project project, @NotNull Collection<GitRepository> repositories) {
    GitFetchSupport fetchSupport = GitFetchSupport.fetchSupport(project);
    return fetchSupport.fetchAllRemotes(repositories);
  }
}
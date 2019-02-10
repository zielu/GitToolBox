package zielu.gittoolbox.fetch;

import com.intellij.openapi.project.Project;
import git4idea.fetch.GitFetchSupport;
import git4idea.repo.GitRepository;
import java.util.Collections;

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
  public static void fetch(GitRepository repository) {
    Project project = repository.getProject();
    GitFetchSupport fetchSupport = GitFetchSupport.fetchSupport(project);
    fetchSupport.fetchAllRemotes(Collections.singleton(repository)).showNotificationIfFailed();
  }
}
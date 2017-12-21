package zielu.gittoolbox.fetch;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import git4idea.update.GitFetcher;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;

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
    GitVcs.runInBackground(new Backgroundable(project, ResBundle.getString("message.fetching"), true) {
      public void run(@NotNull ProgressIndicator indicator) {
        (new GitFetcher(project, indicator, true)).fetchRootsAndNotify(Collections.singleton(repository), null, true);
      }
    });
  }
}

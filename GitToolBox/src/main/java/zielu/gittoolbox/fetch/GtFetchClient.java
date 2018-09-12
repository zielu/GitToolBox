package zielu.gittoolbox.fetch;

import git4idea.repo.GitRepository;
import git4idea.update.GitFetchResult;
import org.jetbrains.annotations.NotNull;

interface GtFetchClient {
  GitFetchResult fetch(@NotNull GitRepository repository);
}

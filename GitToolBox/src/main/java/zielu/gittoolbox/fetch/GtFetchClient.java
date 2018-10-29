package zielu.gittoolbox.fetch;

import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

interface GtFetchClient {
  GtFetchResult fetch(@NotNull GitRepository repository);
}

package zielu.gittoolbox.cache;

import git4idea.repo.GitRepository;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.MemoizeSupplier;

class LazyRepoChangeAware<T extends RepoChangeAware> implements RepoChangeAware {
  private final Supplier<T> supplier;

  LazyRepoChangeAware(Supplier<T> delegateSupplier) {
    supplier = new MemoizeSupplier<>(delegateSupplier);
  }

  @Override
  public void repoChanged(@NotNull GitRepository repository) {
    supplier.get().repoChanged(repository);
  }
}

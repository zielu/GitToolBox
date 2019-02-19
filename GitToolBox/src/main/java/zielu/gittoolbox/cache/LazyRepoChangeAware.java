package zielu.gittoolbox.cache;

import git4idea.repo.GitRepository;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

class LazyRepoChangeAware<T extends RepoChangeAware> implements RepoChangeAware {
  private final Supplier<T> delegateSupplier;
  private volatile T delegate;

  LazyRepoChangeAware(Supplier<T> delegateSupplier) {
    this.delegateSupplier = delegateSupplier;
  }

  private T getDelegate() {
    if (delegate == null) {
      return getDelegateSafe();
    } else {
      return delegate;
    }
  }

  private synchronized T getDelegateSafe() {
    if (delegate == null) {
      delegate = delegateSupplier.get();
      return delegate;
    } else {
      return delegate;
    }
  }


  @Override
  public void repoChanged(@NotNull GitRepository repository) {
    getDelegate().repoChanged(repository);
  }
}

package zielu.gittoolbox.cache;

import com.google.common.collect.ImmutableList;
import git4idea.repo.GitRepository;
import java.util.function.Supplier;

class LazyDirMappingAware<T extends DirMappingAware> implements DirMappingAware {
  private final Supplier<T> delegateSupplier;
  private volatile T delegate;

  LazyDirMappingAware(Supplier<T> delegateSupplier) {
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
  public void updatedRepoList(ImmutableList<GitRepository> repositories) {
    getDelegate().updatedRepoList(repositories);
  }
}

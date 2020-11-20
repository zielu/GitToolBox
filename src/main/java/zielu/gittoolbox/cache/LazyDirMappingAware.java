package zielu.gittoolbox.cache;

import com.google.common.collect.ImmutableList;
import git4idea.repo.GitRepository;
import java.util.function.Supplier;
import zielu.gittoolbox.util.MemoizeSupplier;

class LazyDirMappingAware<T extends DirMappingAware> implements DirMappingAware {
  private final Supplier<T> supplier;

  LazyDirMappingAware(Supplier<T> delegateSupplier) {
    supplier = new MemoizeSupplier<>(delegateSupplier);
  }

  @Override
  public void updatedRepoList(ImmutableList<GitRepository> repositories) {
    supplier.get().updatedRepoList(repositories);
  }
}

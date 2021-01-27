package zielu.gittoolbox.cache;

import git4idea.repo.GitRepository;
import java.util.List;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.MemoizeSupplier;

class LazyDirMappingAware<T extends DirMappingAware> implements DirMappingAware {
  private final Supplier<T> supplier;

  LazyDirMappingAware(Supplier<T> delegateSupplier) {
    supplier = new MemoizeSupplier<>(delegateSupplier);
  }

  @Override
  public void updatedRepoList(@NotNull List<GitRepository> repositories) {
    supplier.get().updatedRepoList(repositories);
  }
}

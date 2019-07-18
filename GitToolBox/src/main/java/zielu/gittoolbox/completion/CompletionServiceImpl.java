package zielu.gittoolbox.completion;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import git4idea.repo.GitRepository;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.formatter.Formatter;

class CompletionServiceImpl implements CompletionService, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final CompletionGateway gateway;
  private WeakReference<CompletionScopeProvider> scopeProviderRef;
  private List<Formatter> formatters;

  CompletionServiceImpl(@NotNull CompletionGateway gateway) {
    this.gateway = gateway;
    formatters = gateway.getFormatters();
    gateway.disposeWithProject(this);
  }

  public void onConfigChanged(@NotNull GitToolBoxConfigForProject config) {
    formatters = ImmutableList.copyOf(config.getCompletionFormatters());
  }

  @Override
  public void setScopeProvider(@NotNull CompletionScopeProvider scopeProvider) {
    log.debug("Set scope provider: ", scopeProvider);
    scopeProviderRef = new WeakReference<>(scopeProvider);
  }

  @Override
  @NotNull
  public Collection<GitRepository> getAffected() {
    CompletionScopeProvider scopeProvider = getScopeProvider();
    Collection<File> affectedFiles = scopeProvider.getAffectedFiles();
    log.debug("Get affected files: ", affectedFiles);
    Collection<GitRepository> affectedRepositories = findAffectedRepositories(affectedFiles);
    log.debug("Get affected repositories: ", affectedRepositories);
    return affectedRepositories;
  }

  private CompletionScopeProvider getScopeProvider() {
    if (scopeProviderRef != null) {
      CompletionScopeProvider provider = scopeProviderRef.get();
      if (provider != null) {
        return provider;
      }
    }
    return CompletionScopeProvider.EMPTY;
  }

  private Collection<GitRepository> findAffectedRepositories(Collection<File> affectedFiles) {
    return gateway.metrics().timer("completion-get-repos")
      .timeSupplier(() -> gateway.getRepositories(affectedFiles));
  }

  @Override
  @NotNull
  public List<Formatter> getFormatters() {
    return formatters;
  }

  @Override
  public void dispose() {
    formatters = null;
    scopeProviderRef = null;
  }
}

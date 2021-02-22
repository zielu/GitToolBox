package zielu.gittoolbox.completion;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;
import zielu.gittoolbox.formatter.Formatter;

class CompletionServiceImpl implements CompletionService, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final CompletionFacade facade;
  private WeakReference<CompletionScopeProvider> scopeProviderRef;
  private volatile ImmutableList<Formatter> formatters;

  CompletionServiceImpl(@NotNull Project project) {
    facade = new CompletionFacade(project);
  }

  @Override
  public void onConfigChanged(@NotNull GitToolBoxConfigPrj config) {
    synchronized (this) {
      formatters = null;
    }
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
    return facade.getRepositories(affectedFiles);
  }

  @Override
  @NotNull
  public List<Formatter> getFormatters() {
    if (formatters == null) {
      synchronized (this) {
        if (formatters == null) {
          formatters = facade.getFormatters();
        }
      }
    }
    return formatters;
  }

  @Override
  public void dispose() {
    formatters = null;
    scopeProviderRef = null;
  }
}

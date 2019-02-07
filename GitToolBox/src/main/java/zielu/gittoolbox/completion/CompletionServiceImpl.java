package zielu.gittoolbox.completion;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.compat.GitCompatUtil;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.formatter.Formatter;
import zielu.gittoolbox.metrics.ProjectMetrics;

class CompletionServiceImpl implements ProjectComponent, CompletionService {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;
  private WeakReference<CompletionScopeProvider> scopeProviderRef;
  private MessageBusConnection connection;
  private List<Formatter> formatters = Collections.emptyList();

  CompletionServiceImpl(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public void initComponent() {
    connectToMessageBus();
  }

  private void connectToMessageBus() {
    connection = project.getMessageBus().connect();
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier() {
      @Override
      public void configChanged(Project project, GitToolBoxConfigForProject previous,
                                GitToolBoxConfigForProject current) {
        onConfigChanged(current);
      }
    });
  }

  private void onConfigChanged(GitToolBoxConfigForProject config) {
    fillFormatters(config);
  }

  private void fillFormatters(GitToolBoxConfigForProject config) {
    formatters = ImmutableList.copyOf(config.getCompletionFormatters());
  }

  @Override
  public void projectOpened() {
    fillFormatters(GitToolBoxConfigForProject.getInstance(project));
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
    CompletionScopeProvider scopeProvider = CompletionScopeProvider.EMPTY;
    if (scopeProviderRef != null) {
      CompletionScopeProvider provider = scopeProviderRef.get();
      if (provider != null) {
        return provider;
      }
    }
    return scopeProvider;
  }

  private Collection<GitRepository> findAffectedRepositories(Collection<File> affectedFiles) {
    return ProjectMetrics.getInstance(project).timer("completion-get-repos")
      .timeSupplier(() -> getRepositories(project, affectedFiles));
  }

  private Collection<GitRepository> getRepositories(Project project, Collection<File> selectedFiles) {
    return GitCompatUtil.getRepositoriesForFiles(project, selectedFiles);
  }

  @Override
  @NotNull
  public List<Formatter> getFormatters() {
    return formatters;
  }

  @Override
  public void projectClosed() {
    if (scopeProviderRef != null) {
      scopeProviderRef.clear();
      scopeProviderRef = null;
    }
  }

  @Override
  public void disposeComponent() {
    disconnectFromMessageBus();
    clearFormatters();
  }

  private void disconnectFromMessageBus() {
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
  }

  private void clearFormatters() {
    formatters = null;
  }
}

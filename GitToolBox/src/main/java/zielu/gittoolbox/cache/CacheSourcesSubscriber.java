package zielu.gittoolbox.cache;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;

class CacheSourcesSubscriber implements BaseComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final AtomicBoolean active = new AtomicBoolean();
  private final Project project;
  private List<DirMappingAware> dirMappingAwares = new ArrayList<>();
  private List<RepoChangeAware> repoChangeAwares = new ArrayList<>();
  private final MessageBusConnection connection;

  CacheSourcesSubscriber(@NotNull Project project) {
    this.project = project;
    connection = project.getMessageBus().connect();
  }

  @Override
  public void initComponent() {
    if (active.compareAndSet(false, true)) {
      registerOrderedAwares();
      subscribeToMessageBus();
    }
  }

  private void registerOrderedAwares() {
    dirMappingAwares.add(new LazyDirMappingAware<>(() -> VirtualFileRepoCache.getInstance(project)));
    Supplier<PerRepoInfoCache> infoCacheSupplier = () -> PerRepoInfoCache.getInstance(project);
    dirMappingAwares.add(new LazyDirMappingAware<>(infoCacheSupplier));
    repoChangeAwares.add(new LazyRepoChangeAware<>(infoCacheSupplier));
  }

  private void subscribeToMessageBus() {
    connection.subscribe(GitRepository.GIT_REPO_CHANGE, this::onRepoChanged);
    connection.subscribe(ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED, this::onDirMappingChanged);
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier() {
      @Override
      public void configChanged(Project project, GitToolBoxConfigForProject previous,
                                GitToolBoxConfigForProject current) {
        if (Objects.equals(CacheSourcesSubscriber.this.project, project)) {
          onConfigChanged(project, previous, current);
        }
      }
    });
  }

  @Override
  public void disposeComponent() {
    if (active.compareAndSet(true, false)) {
      connection.disconnect();
      clearAwares();
    }
  }

  private void clearAwares() {
    dirMappingAwares.clear();
    repoChangeAwares.clear();
  }

  private void onRepoChanged(@NotNull GitRepository repository) {
    if (active.get()) {
      notifyRepoChangeAwares(repository);
    }
  }

  private void notifyRepoChangeAwares(@NotNull GitRepository repository) {
    log.debug("Repo changed: ", repository);
    repoChangeAwares.forEach(aware -> aware.repoChanged(repository));
    log.debug("Repo changed notification done: ", repository);
  }

  private void onDirMappingChanged() {
    if (active.get()) {
      notifyDirMappingChanged();
    }
  }

  private void notifyDirMappingChanged() {
    log.debug("Dir mappings changed");
    GitRepositoryManager gitManager = GitRepositoryManager.getInstance(project);
    ImmutableList<GitRepository> repositories = ImmutableList.copyOf(gitManager.getRepositories());
    dirMappingAwares.forEach(aware -> aware.updatedRepoList(repositories));
    log.debug("Dir mappings change notification done");
  }

  private void onConfigChanged(@NotNull Project project, GitToolBoxConfigForProject previous,
                               @NotNull GitToolBoxConfigForProject current) {
    if (previous.referencePointForStatus.isChanged(current.referencePointForStatus)) {
      GitRepositoryManager gitManager = GitRepositoryManager.getInstance(project);
      ImmutableList.copyOf(gitManager.getRepositories()).forEach(repo ->
          repoChangeAwares.forEach(aware -> aware.repoChanged(repo)));
    }
  }
}

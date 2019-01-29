package zielu.gittoolbox.cache;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.config.ReferencePointForStatusConfig;
import zielu.gittoolbox.config.ReferencePointForStatusType;

class CacheSourcesSubscriber implements ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final AtomicBoolean active = new AtomicBoolean();
  private final Project project;
  private List<DirMappingAware> dirMappingAwares = new ArrayList<>();
  private List<RepoChangeAware> repoChangeAwares = new ArrayList<>();
  private MessageBusConnection connection;
  private ReferencePointForStatusConfig knownRefPointConfig;

  CacheSourcesSubscriber(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public void initComponent() {
    registerOrderedAwares();
    connectToMessageBus();
  }

  private void registerOrderedAwares() {
    VirtualFileRepoCache repoCache = VirtualFileRepoCache.getInstance(project);
    dirMappingAwares.add(repoCache);
    PerRepoInfoCache infoCache = PerRepoInfoCache.getInstance(project);
    dirMappingAwares.add(infoCache);
    repoChangeAwares.add(infoCache);
  }

  private void connectToMessageBus() {
    MessageBus messageBus = project.getMessageBus();
    connection = messageBus.connect();
    connection.subscribe(GitRepository.GIT_REPO_CHANGE, this::onRepoChanged);
    connection.subscribe(ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED, this::onDirMappingChanged);
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier.Adapter() {
      @Override
      public void configChanged(Project project, GitToolBoxConfigForProject config) {
        if (Objects.equals(CacheSourcesSubscriber.this.project, project)) {
          onConfigChanged(project, config);
        }
      }
    });
  }

  @Override
  public void projectOpened() {
    if (active.compareAndSet(false, true)) {
      knownRefPointConfig = GitToolBoxConfigForProject.getInstance(project).referencePointForStatus.copy();
    }
  }

  @Override
  public void projectClosed() {
    active.compareAndSet(true, false);
  }

  @Override
  public void disposeComponent() {
    disconnectFromMessageBus();
    clearAwares();
  }

  private void disconnectFromMessageBus() {
    connection.disconnect();
    connection = null;
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

  private void onConfigChanged(@NotNull Project project, @NotNull GitToolBoxConfigForProject config) {
    ReferencePointForStatusConfig currentRefPointConfig = config.referencePointForStatus;
    if (currentRefPointConfig.isChanged(knownRefPointConfig)) {
      knownRefPointConfig = currentRefPointConfig.copy();
      GitRepositoryManager gitManager = GitRepositoryManager.getInstance(project);
      ImmutableList.copyOf(gitManager.getRepositories()).forEach(repo ->
          repoChangeAwares.forEach(aware ->
              aware.repoChanged(repo)));
    }
  }
}

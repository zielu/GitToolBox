package zielu.gittoolbox.cache;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public class CacheSourcesSubscriber implements ProjectComponent {
  private final AtomicBoolean active = new AtomicBoolean();
  private final Project project;
  private List<DirMappingAware> dirMappingAwares = new ArrayList<>();
  private List<RepoChangeAware> repoChangeAwares = new ArrayList<>();
  private MessageBusConnection connection;

  public CacheSourcesSubscriber(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public void initComponent() {
    //order is significant
    VirtualFileRepoCache repoCache = VirtualFileRepoCache.getInstance(project);
    dirMappingAwares.add(repoCache);
    PerRepoInfoCache infoCache = PerRepoInfoCache.getInstance(project);
    dirMappingAwares.add(infoCache);
    repoChangeAwares.add(infoCache);

    MessageBus messageBus = project.getMessageBus();
    connection = messageBus.connect();
    connection.subscribe(GitRepository.GIT_REPO_CHANGE, this::repoChanged);
    connection.subscribe(ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED, this::dirMappingChanged);
  }

  @Override
  public void projectOpened() {
    active.compareAndSet(false, true);
  }

  @Override
  public void projectClosed() {
    active.compareAndSet(true, false);
  }

  @Override
  public void disposeComponent() {
    connection.disconnect();
    connection = null;
  }

  private void repoChanged(@NotNull GitRepository repository) {
    if (active.get()) {
      repoChangeAwares.forEach(aware -> aware.repoChanged(repository));
    }
  }

  private void dirMappingChanged() {
    if (active.get()) {
      GitRepositoryManager gitManager = GitRepositoryManager.getInstance(project);
      ImmutableList<GitRepository> repositories = ImmutableList.copyOf(gitManager.getRepositories());
      dirMappingAwares.forEach(aware -> aware.updatedRepoList(repositories));
    }
  }
}

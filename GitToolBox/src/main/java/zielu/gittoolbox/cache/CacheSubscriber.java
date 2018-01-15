package zielu.gittoolbox.cache;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public class CacheSubscriber implements ProjectComponent {
  public static final Topic<CacheSubscriptionListener> SUBSCRIBER_CHANGE = Topic.create(
      "Cache subscriber change", CacheSubscriptionListener.class);
  private final AtomicBoolean active = new AtomicBoolean();
  private final Project project;
  private final MessageBus messageBus;
  private final MessageBusConnection connection;

  public CacheSubscriber(@NotNull Project project) {
    this.project = project;
    messageBus = project.getMessageBus();
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
  }

  private void repoChanged(@NotNull GitRepository repository) {
    if (active.get()) {
      messageBus.syncPublisher(SUBSCRIBER_CHANGE).repoChanged(repository);
    }
  }

  private void dirMappingChanged() {
    if (active.get()) {
      GitRepositoryManager gitManager = GitRepositoryManager.getInstance(project);
      ImmutableList<GitRepository> repositories = ImmutableList.copyOf(gitManager.getRepositories());
      messageBus.syncPublisher(SUBSCRIBER_CHANGE).dirMappingChanged(repositories);
    }
  }
}

package zielu.gittoolbox.repo;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryChangeListener;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

public class GtRepositoryManager implements ProjectComponent, GitRepositoryChangeListener {
  private final Map<GitRepository, GtConfig> configs = new ConcurrentHashMap<GitRepository, GtConfig>();
  private final Project project;
  private MessageBusConnection connection;

  public GtRepositoryManager(@NotNull Project project) {
    this.project = project;
  }

  @NotNull
  public static GtRepositoryManager getInstance(@NotNull Project project) {
    return project.getComponent(GtRepositoryManager.class);
  }

  @Override
  public void repositoryChanged(@NotNull GitRepository repository) {
    VirtualFile gitDir = GitUtil.findGitDir(repository.getRoot());
    Optional.ofNullable(gitDir)
        .map(dir -> dir.findChild("config"))
        .map(VfsUtilCore::virtualToIoFile)
        .map(GtConfig::load)
        .ifPresent(config -> configs.put(repository, config));
  }

  public Optional<GtConfig> configFor(GitRepository repository) {
    return Optional.ofNullable(configs.get(repository));
  }

  @Override
  public void initComponent() {
    connection = project.getMessageBus().connect();
    connection.subscribe(GitRepository.GIT_REPO_CHANGE, this);
  }

  @Override
  public void disposeComponent() {
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
    configs.clear();
  }
}

package zielu.gittoolbox.repo;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.messages.MessageBusConnection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryChangeListener;
import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

public class GtRepositoryManager extends AbstractProjectComponent implements GitRepositoryChangeListener {
  private final Map<GitRepository, GtConfig> configs = new ConcurrentHashMap<GitRepository, GtConfig>();
  private MessageBusConnection connection;

  public GtRepositoryManager(Project project) {
    super(project);
  }

  @SuppressFBWarnings({"NP_NULL_ON_SOME_PATH"})
  @NotNull
  public static GtRepositoryManager getInstance(@NotNull Project project) {
    return project.getComponent(GtRepositoryManager.class);
  }

  @SuppressFBWarnings({"NP_NULL_ON_SOME_PATH"})
  @Override
  public void repositoryChanged(@NotNull GitRepository repository) {
    File configFile = new File(VfsUtilCore.virtualToIoFile(repository.getGitDir()), "config");
    GtConfig config = GtConfig.load(configFile);
    configs.put(repository, config);
  }

  public java.util.Optional<GtConfig> configFor(GitRepository repository) {
    return Optional.ofNullable(configs.get(repository));
  }

  @Override
  public void initComponent() {
    connection = myProject.getMessageBus().connect();
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

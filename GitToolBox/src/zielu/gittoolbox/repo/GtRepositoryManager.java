package zielu.gittoolbox.repo;

import com.google.common.base.Optional;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryChangeListener;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

public class GtRepositoryManager extends AbstractProjectComponent implements GitRepositoryChangeListener {
    private final Map<GitRepository, GtConfig> myConfigs = new ConcurrentHashMap<GitRepository, GtConfig>();
    private MessageBusConnection myConnection;

    public GtRepositoryManager(Project project) {
        super(project);
    }

    @Override
    public void repositoryChanged(@NotNull GitRepository repository) {
        File configFile = new File(VfsUtilCore.virtualToIoFile(repository.getGitDir()), "config");
        GtConfig config = GtConfig.load(configFile);
        myConfigs.put(repository, config);
    }

    public Optional<GtConfig> configFor(GitRepository repository) {
        return Optional.fromNullable(myConfigs.get(repository));
    }

    @Override
    public void initComponent() {
        myConnection = myProject.getMessageBus().connect();
        myConnection.subscribe(GitRepository.GIT_REPO_CHANGE, this);
    }

    @Override
    public void disposeComponent() {
        if (myConnection != null) {
            myConnection.disconnect();
            myConnection = null;
        }
        myConfigs.clear();
    }

    public static GtRepositoryManager getInstance(@NotNull Project project) {
        return project.getComponent(GtRepositoryManager.class);
    }
}

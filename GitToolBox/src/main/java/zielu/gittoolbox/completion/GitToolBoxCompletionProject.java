package zielu.gittoolbox.completion;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import git4idea.repo.GitRepository;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.compat.GitCompatUtil;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.ConfigNotifier.Adapter;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.formatter.Formatter;
import zielu.gittoolbox.util.diagnostics.PerfWatch;

public class GitToolBoxCompletionProject extends AbstractProjectComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final List<File> affectedFiles = new ArrayList<>();
  private Collection<GitRepository> affectedRepositories;
  private MessageBusConnection connection;
  private List<Formatter> formatters = ImmutableList.of();

  public GitToolBoxCompletionProject(@NotNull Project project) {
    super(project);
  }

  @SuppressFBWarnings({"NP_NULL_ON_SOME_PATH"})
  @NotNull
  public static GitToolBoxCompletionProject getInstance(@NotNull Project project) {
    return project.getComponent(GitToolBoxCompletionProject.class);
  }

  @Override
  public void initComponent() {
    connection = myProject.getMessageBus().connect();
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new Adapter() {
      @Override
      public void configChanged(Project project, GitToolBoxConfigForProject config) {
        fillFormatters(config);
      }
    });
  }

  private void fillFormatters(GitToolBoxConfigForProject config) {
    formatters = ImmutableList.copyOf(config.getCompletionFormatters());
  }

  @Override
  public void projectOpened() {
    fillFormatters(GitToolBoxConfigForProject.getInstance(myProject));
  }

  public synchronized void updateAffected(Collection<File> affected) {
    clearAffected();
    affectedFiles.addAll(affected);
  }

  public synchronized void clearAffected() {
    affectedRepositories = null;
    affectedFiles.clear();
  }

  public synchronized Collection<GitRepository> getAffected() {
    if (affectedRepositories == null) {
      PerfWatch getRepositoriesWatch = PerfWatch.createStarted("Get repositories");
      affectedRepositories = getRepositories(myProject, affectedFiles);
      getRepositoriesWatch.finish();
    }
    return affectedRepositories;
  }

  private Collection<GitRepository> getRepositories(Project project, Collection<File> selectedFiles) {
    return GitCompatUtil.getRepositoriesForFiles(project, selectedFiles);
  }

  public List<Formatter> getFormatters() {
    return formatters;
  }

  @Override
  public void projectClosed() {
    clearAffected();
  }

  @Override
  public void disposeComponent() {
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
    formatters = null;
  }
}

package zielu.gittoolbox.completion;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
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
import zielu.gittoolbox.metrics.MetricsHost;

public class GitToolBoxCompletionProject implements ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final List<File> affectedFiles = new ArrayList<>();
  private final Project project;
  private Collection<GitRepository> affectedRepositories;
  private MessageBusConnection connection;
  private List<Formatter> formatters = ImmutableList.of();

  public GitToolBoxCompletionProject(@NotNull Project project) {
    this.project = project;
  }

  @NotNull
  public static GitToolBoxCompletionProject getInstance(@NotNull Project project) {
    return project.getComponent(GitToolBoxCompletionProject.class);
  }

  @Override
  public void initComponent() {
    connectToMessageBus();
  }

  private void connectToMessageBus() {
    connection = project.getMessageBus().connect();
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new Adapter() {
      @Override
      public void configChanged(Project project, GitToolBoxConfigForProject config) {
        onConfigChanged(config);
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
      affectedRepositories = findAffectedRepositories();
    }
    return affectedRepositories;
  }

  private Collection<GitRepository> findAffectedRepositories() {
    return MetricsHost.project(project).timer("completion-get-repos")
      .timeSupplier(() -> getRepositories(project, affectedFiles));
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
    disconnectFromMessageBus();
    clearFormatters();
    clearAffected();
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

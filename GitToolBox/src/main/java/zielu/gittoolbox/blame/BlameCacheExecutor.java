package zielu.gittoolbox.blame;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ProjectGateway;
import zielu.gittoolbox.util.ExecutableTask;

class BlameCacheExecutor implements Disposable {
  private final Project project;
  private final ExecutorService executor;

  BlameCacheExecutor(@NotNull Project project, @NotNull ProjectGateway gateway) {
    this.project = project;
    executor = Executors.newCachedThreadPool(
        new ThreadFactoryBuilder().setNameFormat("Blame-" + project.getName() + "-%d").setDaemon(true).build()
    );
    gateway.disposeWithProject(this);
  }

  void execute(ExecutableTask executable) {
    executeInThreadPool(executable);
  }

  private void executeWithProgress(ExecutableTask executable) {
    Task.Backgroundable task = new Task.Backgroundable(project, executable.getTitle()) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        indicator.setIndeterminate(true);
        executable.run();
      }
    };
    task.queue();
  }

  private void executeInThreadPool(ExecutableTask executableTask) {
    executor.submit(executableTask::run);
  }

  @Override
  public void dispose() {
    executor.shutdownNow();
  }
}

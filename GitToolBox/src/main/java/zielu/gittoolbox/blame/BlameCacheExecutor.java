package zielu.gittoolbox.blame;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.FeatureToggles;
import zielu.gittoolbox.util.ExecutableTask;

class BlameCacheExecutor implements Disposable {
  private final Project project;
  private final BlameCacheGateway gateway;
  private final ExecutorService executor;
  private final Consumer<ExecutableTask> execution;

  BlameCacheExecutor(@NotNull Project project, @NotNull BlameCacheGateway gateway) {
    this.project = project;
    this.gateway = gateway;
    if (FeatureToggles.showBlameProgress()) {
      executor = null;
      execution = this::executeWithProgress;
    } else {
      executor = Executors.newCachedThreadPool(
          new ThreadFactoryBuilder().setNameFormat("Blame-" + project.getName() + "-%d").setDaemon(true).build()
      );
      execution = this::executeInThreadPool;
    }
    gateway.disposeWithProject(this);
  }

  void execute(ExecutableTask executable) {
    gateway.runInBackground(() -> execution.accept(executable));
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
    if (executor != null) {
      executor.shutdownNow();
    }
  }
}

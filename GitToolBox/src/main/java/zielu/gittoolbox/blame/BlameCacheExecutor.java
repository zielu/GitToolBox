package zielu.gittoolbox.blame;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.ProjectMetrics;
import zielu.gittoolbox.util.AppUtil;
import zielu.gittoolbox.util.ExecutableTask;

class BlameCacheExecutor implements Disposable {
  private static final int MAX_ALLOWED = 2;
  private final Semaphore activeTasks = new Semaphore(MAX_ALLOWED);
  private final AtomicBoolean active = new AtomicBoolean(true);
  private final Queue<ExecutableTask> tasks = new LinkedBlockingQueue<>();
  private final Project project;
  private final Consumer<ExecutableTask> execution;

  BlameCacheExecutor(@NotNull Project project) {
    this.project = project;
    execution = this::executeWithProgress;
    Metrics metrics = ProjectMetrics.getInstance(project);
    metrics.gauge("blame-cache.executor.queue.size", tasks::size);
    metrics.gauge("blame-cache.executor.max-allowed", () -> MAX_ALLOWED);
    metrics.gauge("blame-cache.executor.permits.count", activeTasks::availablePermits);
  }

  @NotNull
  static Optional<BlameCacheExecutor> getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstanceSafe(project, BlameCacheExecutor.class);
  }

  void execute(ExecutableTask executable) {
    if (active.get()) {
      if (activeTasks.tryAcquire()) {
        execution.accept(executable);
      } else {
        tasks.offer(executable);
      }
    }
  }

  private void executeNext() {
    ExecutableTask executableTask = tasks.poll();
    if (executableTask != null) {
      execute(executableTask);
    }
  }

  private void executeWithProgress(ExecutableTask executable) {
    Task.Backgroundable task = new Task.Backgroundable(project, executable.getTitle()) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        indicator.setIndeterminate(true);
        if (active.get()) {
          executable.run();
        }
      }

      @Override
      public void onFinished() {
        activeTasks.release();
        executeNext();
      }
    };
    if (active.get()) {
      task.queue();
    }
  }

  @Override
  public void dispose() {
    if (active.compareAndSet(true, false)) {
      tasks.clear();
    }
  }
}

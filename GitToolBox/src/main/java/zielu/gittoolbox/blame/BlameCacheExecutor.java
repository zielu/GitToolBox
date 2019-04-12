package zielu.gittoolbox.blame;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.FeatureToggles;
import zielu.gittoolbox.util.ExecutableTask;

class BlameCacheExecutor {
  private final Project project;
  private final Consumer<ExecutableTask> execution;

  BlameCacheExecutor(@NotNull Project project, @NotNull BlameCacheGateway gateway) {
    this.project = project;
    if (FeatureToggles.showBlameProgress()) {
      if (ApplicationManager.getApplication().isUnitTestMode()) {
        //during tests there is no backing thread pool behind Task:queue
        execution = executable -> gateway.runInBackground(() -> executeWithProgress(executable));
      } else {
        execution = this::executeWithProgress;
      }
    } else {
      execution = executable -> gateway.runInBackground(executable::run);
    }
  }

  void execute(ExecutableTask executable) {
    execution.accept(executable);
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
}

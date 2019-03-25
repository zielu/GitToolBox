package zielu.gittoolbox.blame;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.ExecutableTask;

class BlameCacheExecutor {
  private final Project project;

  BlameCacheExecutor(@NotNull Project project) {
    this.project = project;
  }

  void execute(ExecutableTask executable) {
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

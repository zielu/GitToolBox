package zielu.gittoolbox.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Timer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.DisposeSafeRunnable;

class ProjectMetricsImpl implements ProjectMetrics {
  private final MetricManager metrics = new MetricManager();

  ProjectMetricsImpl(@NotNull Project project) {
    project.getMessageBus().connect(project).subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
      @Override
      public void projectOpened(@NotNull Project project) {
        ApplicationManager.getApplication()
            .executeOnPooledThread(new DisposeSafeRunnable(project, () -> startReporter(project)));
      }
    });
  }

  private void startReporter(@NotNull Project project) {
    MetricsReporter reporter = Jmx.reporter(project, metrics.getRegistry());
    Disposer.register(project, reporter);
  }

  @Override
  public Timer timer(@NotNull String simpleName) {
    return metrics.timer(simpleName);
  }

  @Override
  public Counter counter(@NotNull String simpleName) {
    return metrics.counter(simpleName);
  }

  @Override
  public <T> Gauge gauge(@NotNull String simpleName, Gauge<T> gauge) {
    return metrics.gauge(simpleName, gauge);
  }

  @Override
  public void addAll(MetricSet metricSet) {
    metrics.addAll(metricSet);
  }
}

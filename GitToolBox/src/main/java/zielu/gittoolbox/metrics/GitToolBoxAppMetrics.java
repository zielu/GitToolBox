package zielu.gittoolbox.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jmx.JmxReporter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

class GitToolBoxAppMetrics implements ApplicationComponent, Metrics {
  private final MetricManager metrics = new MetricManager();
  private JmxReporter reporter;

  static Metrics getInstance() {
    return ApplicationManager.getApplication().getComponent(GitToolBoxAppMetrics.class);
  }

  private String name(@NotNull String simpleName) {
    return MetricRegistry.name(simpleName);
  }

  @Override
  public void initComponent() {
    reporter = Jmx.report(metrics.getRegistry());
    reporter.start();
  }

  @Override
  public void disposeComponent() {
    Optional.ofNullable(reporter).ifPresent(JmxReporter::close);
    reporter = null;
  }

  @Override
  public Timer timer(@NotNull String simpleName) {
    return metrics.timer(name(simpleName));
  }

  @Override
  public Counter counter(@NotNull String simpleName) {
    return metrics.counter(name(simpleName));
  }

  @Override
  public <T> Gauge<T> gauge(@NotNull String simpleName, Gauge<T> gauge) {
    return metrics.gauge(simpleName, gauge);
  }
}

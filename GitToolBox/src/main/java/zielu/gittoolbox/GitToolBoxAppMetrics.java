package zielu.gittoolbox;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jmx.JmxReporter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jetbrains.annotations.NotNull;

public class GitToolBoxAppMetrics implements ApplicationComponent {
  private static final String PREFIX = "zielu.gittoolbox.";

  private final MetricRegistry registry = new MetricRegistry();
  private final ConcurrentMap<String, Gauge> gauges = new ConcurrentHashMap<>();
  private final JmxReporter reporter = JmxReporter.forRegistry(registry).inDomain("gittoolbox").build();

  public static GitToolBoxAppMetrics getInstance() {
    return ApplicationManager.getApplication().getComponent(GitToolBoxAppMetrics.class);
  }

  @Override
  public void initComponent() {
    reporter.start();
  }

  @Override
  public void disposeComponent() {
    reporter.close();
  }

  public Timer timer(@NotNull String simpleName) {
    return registry.timer(name(simpleName));
  }

  private String name(@NotNull String simpleName) {
    return PREFIX + simpleName;
  }

  public Counter counter(@NotNull String simpleName) {
    return registry.counter(name(simpleName));
  }

  public <T> Gauge<T> gauge(@NotNull String simpleName, Gauge<T> gauge) {
    String name = name(simpleName);
    return gauges.computeIfAbsent(name, gaugeName -> registry.register(name, gauge));
  }
}

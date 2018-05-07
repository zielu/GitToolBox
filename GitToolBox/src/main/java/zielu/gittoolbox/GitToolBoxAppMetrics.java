package zielu.gittoolbox;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jmx.JmxReporter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

public class GitToolBoxAppMetrics implements ApplicationComponent {
  private final MetricRegistry registry = new MetricRegistry();
  private final JmxReporter reporter = JmxReporter.forRegistry(registry).build();

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
    return registry.timer(("zielu_gittoolbox_" + simpleName));
  }
}

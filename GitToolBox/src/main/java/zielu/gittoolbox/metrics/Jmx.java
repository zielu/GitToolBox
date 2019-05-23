package zielu.gittoolbox.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.security.Permission;
import javax.management.MBeanTrustPermission;
import org.jetbrains.annotations.NotNull;

class Jmx {
  private static final String DOMAIN = "zielu.gittoolbox";
  private static final Logger LOG = Logger.getInstance(Jmx.class);

  private Jmx() {
    throw new IllegalStateException();
  }

  static void reporter(MetricRegistry registry) {
    if (shouldExport()) {
      JmxReporter reporter = JmxReporter.forRegistry(registry).inDomain(DOMAIN).build();
      reporter.start();
    }
  }

  static MetricsReporter reporter(@NotNull Project project, MetricRegistry registry) {
    if (shouldExport()) {
      String projectName = project.getName().replaceAll("\\W", "");
      JmxReporter reporter = JmxReporter.forRegistry(registry).inDomain(DOMAIN + "." + projectName).build();
      reporter.start();
      return new JmxMetricsReporter(reporter);
    } else {
      return MetricsReporter.EMPTY;
    }
  }

  private static boolean shouldExport() {
    SecurityManager securityManager = System.getSecurityManager();
    if (securityManager != null) {
      Permission permission = new MBeanTrustPermission("register");
      try {
        securityManager.checkPermission(permission);
        return true;
      } catch (SecurityException e) {
        LOG.warn("Cannot export JMX metrics", e);
        return false;
      }
    }
    return true;
  }

  private static class JmxMetricsReporter implements MetricsReporter {
    private final JmxReporter reporter;

    private JmxMetricsReporter(JmxReporter reporter) {
      this.reporter = reporter;
    }

    @Override
    public void dispose() {
      reporter.close();
    }
  }
}

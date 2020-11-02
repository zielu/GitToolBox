package zielu.gittoolbox.metrics

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.jmx.JmxReporter
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.security.Permission
import javax.management.MBeanTrustPermission

internal object Jmx {
  private const val DOMAIN = "zielu.gittoolbox"
  private val log = Logger.getInstance(Jmx::class.java)

  @JvmStatic
  fun startReporting(registry: MetricRegistry): MetricsReporter {
    return if (shouldExport()) {
      val reporter = JmxReporter.forRegistry(registry).inDomain(DOMAIN).build()
      reporter.start()
      JmxMetricsReporter(reporter)
    } else {
      MetricsReporter.EMPTY
    }
  }

  @JvmStatic
  fun startReporting(project: Project, registry: MetricRegistry): MetricsReporter {
    return if (shouldExport()) {
      val projectName = project.name.replace("\\W".toRegex(), "")
      val reporter = JmxReporter.forRegistry(registry).inDomain("$DOMAIN.$projectName").build()
      reporter.start()
      JmxMetricsReporter(reporter)
    } else {
      MetricsReporter.EMPTY
    }
  }

  private fun shouldExport(): Boolean {
    return System.getSecurityManager()?.let { securityManager ->
      val permission: Permission = MBeanTrustPermission("register")
      return try {
        securityManager.checkPermission(permission)
        true
      } catch (e: SecurityException) {
        log.warn("Cannot export JMX metrics", e)
        false
      }
    } ?: true
  }
}

private class JmxMetricsReporter(private val reporter: JmxReporter) : MetricsReporter {
  private val log = Logger.getInstance(JmxMetricsReporter::class.java)

  override fun dispose() {
    reporter.close()
    log.info("Disposed")
  }
}

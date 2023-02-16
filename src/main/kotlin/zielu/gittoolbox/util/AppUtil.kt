package zielu.gittoolbox.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.config.GitToolBoxConfig2
import java.lang.IllegalStateException
import java.util.Optional

internal object AppUtil {
  private val log = Logger.getInstance(AppUtil::class.java)

  @JvmStatic
  fun <T> getExistingServiceInstance(project: Project, serviceType: Class<T>): Optional<T> {
    return Optional.ofNullable(project.getServiceIfCreated(serviceType))
  }

  @JvmStatic
  fun <T> getExistingServiceInstance(serviceType: Class<T>): Optional<T> {
    return Optional.ofNullable(ApplicationManager.getApplication().getServiceIfCreated(serviceType))
  }

  @JvmStatic
  fun <T> getServiceInstance(project: Project, serviceType: Class<T>): T {
    return project.getService(serviceType)
      ?: throw IllegalStateException("Service ${serviceType.name} not found")
  }

  @JvmStatic
  fun <T> getServiceInstanceSafe(project: Project, serviceType: Class<T>): Optional<T> {
    return if (project.isDisposed) {
      Optional.empty()
    } else {
      Optional.ofNullable(project.getService(serviceType))
    }
  }

  @JvmStatic
  fun <T> getServiceInstance(serviceType: Class<T>): T {
    return ApplicationManager.getApplication().getService(serviceType)
      ?: throw IllegalStateException("Service ${serviceType.name} not found")
  }

  @JvmStatic
  fun <T> getServiceInstanceSafe(serviceType: Class<T>): Optional<T> {
    return Optional.ofNullable(ApplicationManager.getApplication().getService(serviceType))
  }

  @JvmStatic
  fun <T> runReadAction(block: () -> T): T {
    return ApplicationManager.getApplication().runReadAction<T> { block.invoke() }
  }

  fun hasUi(): Boolean {
    return !ApplicationManager.getApplication().isHeadlessEnvironment
  }

  @JvmStatic
  fun updateSettingsAndSave(modify: (GitToolBoxConfig2) -> Unit) {
    val application = ApplicationManager.getApplication()
    if (!application.isUnitTestMode) {
      log.info("Saving settings")
      try {
        WriteAction.runAndWait<RuntimeException> {
          val current = AppConfig.getConfig()
          modify.invoke(current)
          AppConfig.getInstance().stateUpdated(current)
          application.saveSettings()
        }
      } catch (exception: java.lang.Exception) {
        log.error("Failed to save settings", exception)
      }
    }
  }
}

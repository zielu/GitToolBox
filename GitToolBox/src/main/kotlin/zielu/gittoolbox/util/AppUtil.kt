package zielu.gittoolbox.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.ThrowableRunnable
import java.util.Optional

internal object AppUtil {
  private val log = Logger.getInstance(AppUtil::class.java)

  @JvmStatic
  fun <T> getExistingServiceInstance(project: Project, serviceType: Class<T>): Optional<T> {
    return Optional.ofNullable(ServiceManager.getServiceIfCreated(project, serviceType))
  }

  @JvmStatic
  fun <T> getServiceInstance(project: Project, serviceType: Class<T>): T {
    return ServiceManager.getService(project, serviceType)
  }

  @JvmStatic
  fun <T> getServiceInstance(serviceType: Class<T>): T {
    return ServiceManager.getService(serviceType)
  }

  @JvmStatic
  fun <T> getComponentInstance(project: Project, componentType: Class<T>): T {
    return project.getComponent(componentType)
  }

  fun <T> runReadAction(block: () -> T): T {
    return ApplicationManager.getApplication().runReadAction<T> { block.invoke() }
  }

  fun hasUi(): Boolean {
    return !ApplicationManager.getApplication().isHeadlessEnvironment
  }

  fun saveAppSettings() {
    val application = ApplicationManager.getApplication()
    if (!application.isUnitTestMode) {
      log.info("Saving app settings")
      try {
        WriteAction.runAndWait(ThrowableRunnable<Exception> { application.saveSettings() })
      } catch (exception: Exception) {
        log.error("Failed to save settings", exception)
      }
    }
  }
}

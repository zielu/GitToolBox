package zielu.gittoolbox.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.ThrowableRunnable
import zielu.gittoolbox.config.AppConfig.get
import zielu.gittoolbox.config.GitToolBoxConfig2
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
  }

  fun <T> getServiceInstanceSafe(project: Project, serviceType: Class<T>): Optional<T> {
    return Optional.ofNullable(project.getService(serviceType))
  }

  @JvmStatic
  fun <T> getServiceInstance(serviceType: Class<T>): T {
    return ApplicationManager.getApplication().getService(serviceType)
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

  @JvmStatic
  fun modifySettingsSaveAndNotify(modify: (GitToolBoxConfig2) -> Unit) {
    val application = ApplicationManager.getApplication()
    if (!application.isUnitTestMode) {
      log.info("Saving settings")
      try {
        WriteAction.runAndWait<RuntimeException> {
          val current = get()
          val before = current.copy()
          modify.invoke(current)
          application.saveSettings()
          current.fireChanged(before)
        }
      } catch (exception: java.lang.Exception) {
        log.error("Failed to save settings", exception)
      }
    }
  }
}

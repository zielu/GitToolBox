package zielu.gittoolbox.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import java.util.Optional

internal object AppUtil {
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
}

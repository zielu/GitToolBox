package zielu.intellij.extensions

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.AbstractExtensionPointBean
import com.intellij.openapi.project.Project
import org.slf4j.LoggerFactory

internal abstract class ZAbstractExtensionPointBean : AbstractExtensionPointBean() {
  protected fun <T> createInstance(className: String): T {
    val extension = instantiateClass<T>(className, ApplicationManager.getApplication().picoContainer)
    log.info("Application extension created", extension)
    return extension
  }

  protected fun <T> createInstance(className: String, project: Project): T {
    val extension = instantiateClass<T>(className, project.picoContainer)
    log.info("Project extension created", extension)
    return extension
  }

  companion object {
    private val log = LoggerFactory.getLogger(ZAbstractExtensionPointBean::class.java)
  }
}

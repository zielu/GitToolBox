package zielu.intellij.extensions

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.AbstractExtensionPointBean
import com.intellij.openapi.project.Project

internal abstract class ZAbstractExtensionPointBean : AbstractExtensionPointBean() {
  protected fun <T> createInstance(className: String): T {
    return instantiate<T>(findClass(className), ApplicationManager.getApplication().picoContainer)
  }

  protected fun <T> createInstance(className: String, project: Project): T {
    return instantiate<T>(findClass(className), project.picoContainer)
  }
}

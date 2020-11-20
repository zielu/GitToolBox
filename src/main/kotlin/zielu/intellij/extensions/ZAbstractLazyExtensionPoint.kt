package zielu.intellij.extensions

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.PluginAware
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.project.Project
import com.intellij.serviceContainer.LazyExtensionInstance

internal abstract class ZAbstractLazyExtensionPoint<T> : LazyExtensionInstance<T>(), PluginAware {
  private lateinit var pluginDescriptor: PluginDescriptor

  protected fun createInstance(): T {
    return getInstance(ApplicationManager.getApplication(), pluginDescriptor)
  }

  protected fun createInstance(project: Project): T {
    return getInstance(project, pluginDescriptor)
  }

  override fun setPluginDescriptor(pluginDescriptor: PluginDescriptor) {
    this.pluginDescriptor = pluginDescriptor
  }
}

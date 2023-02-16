package zielu.intellij.extensions

import com.intellij.openapi.extensions.PluginAware
import com.intellij.openapi.extensions.PluginDescriptor

internal abstract class ZTypeExtensionInstance<T> : PluginAware {
  private var pluginDescriptor: PluginDescriptor? = null

  override fun setPluginDescriptor(pluginDescriptor: PluginDescriptor) {
    this.pluginDescriptor = pluginDescriptor
  }

  protected abstract fun getClassName(): String

  protected fun findClass(): Class<T> {
    val classLoader = pluginDescriptor?.pluginClassLoader ?: javaClass.classLoader
    return Class.forName(getClassName(), true, classLoader) as Class<T>
  }
}

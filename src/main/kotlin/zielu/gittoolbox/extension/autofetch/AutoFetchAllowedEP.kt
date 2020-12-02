package zielu.gittoolbox.extension.autofetch

import com.intellij.util.xmlb.annotations.Attribute
import zielu.intellij.extensions.ZAbstractLazyExtensionPoint

internal class AutoFetchAllowedEP : ZAbstractLazyExtensionPoint<AutoFetchAllowed>() {
  @Attribute("provider")
  lateinit var provider: String

  override fun getImplementationClassName(): String = provider

  fun instantiate(): AutoFetchAllowed = createInstance()
}

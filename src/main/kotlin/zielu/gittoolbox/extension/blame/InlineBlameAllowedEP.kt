package zielu.gittoolbox.extension.blame

import com.intellij.util.xmlb.annotations.Attribute
import zielu.intellij.extensions.ZAbstractLazyExtensionPoint

internal class InlineBlameAllowedEP : ZAbstractLazyExtensionPoint<InlineBlameAllowed>() {
  @Attribute("provider")
  lateinit var provider: String

  override fun getImplementationClassName(): String = provider

  fun instantiate(): InlineBlameAllowed {
    return createInstance()
  }
}

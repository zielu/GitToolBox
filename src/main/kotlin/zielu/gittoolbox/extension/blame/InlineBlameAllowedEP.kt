package zielu.gittoolbox.extension.blame

import com.intellij.util.xmlb.annotations.Attribute
import zielu.intellij.extensions.ZAbstractExtensionPointBean

internal class InlineBlameAllowedEP : ZAbstractExtensionPointBean() {
  @Attribute("provider")
  lateinit var provider: String

  fun instantiate(): InlineBlameAllowed {
    return createInstance(provider)
  }
}

package zielu.gittoolbox.extension.autofetch

import com.intellij.util.xmlb.annotations.Attribute
import zielu.intellij.extensions.ZAbstractExtensionPointBean

internal class AutoFetchAllowedEP : ZAbstractExtensionPointBean() {
  @Attribute("provider")
  lateinit var provider: String

  fun instantiate(): AutoFetchAllowed {
    return createInstance(provider)
  }
}

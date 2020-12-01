package zielu.gittoolbox.extension.update

import com.intellij.util.xmlb.annotations.Attribute
import zielu.intellij.extensions.ZAbstractExtensionPointBean

internal class UpdateProjectActionEP : ZAbstractExtensionPointBean() {
  @Attribute("provider")
  lateinit var provider: String

  fun instantiate(): UpdateProjectAction {
    return createInstance(provider)
  }
}

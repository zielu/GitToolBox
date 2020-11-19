package zielu.gittoolbox.extension.update

import com.intellij.util.xmlb.annotations.Attribute
import zielu.intellij.extensions.ZAbstractLazyExtensionPoint

internal class UpdateProjectActionEP : ZAbstractLazyExtensionPoint<UpdateProjectAction>() {
  @Attribute("provider")
  lateinit var provider: String

  override fun getImplementationClassName(): String = provider

  fun instantiate(): UpdateProjectAction {
    return createInstance()
  }
}

package zielu.gittoolbox.extension.projectview

import com.intellij.ide.projectView.impl.nodes.AbstractModuleNode
import com.intellij.openapi.extensions.AbstractExtensionPointBean
import com.intellij.util.xmlb.annotations.Attribute

internal class ViewModuleNodeParentEP : AbstractExtensionPointBean() {
  @Attribute("moduleNodeClass")
  lateinit var moduleNodeClass: String

  fun getModuleNodeClass(): Class<AbstractModuleNode> {
    return findClassNoExceptions(moduleNodeClass)!!
  }
}

package zielu.gittoolbox.extension.projectview

import com.intellij.ide.projectView.impl.nodes.AbstractModuleNode
import com.intellij.util.xmlb.annotations.Attribute
import zielu.intellij.extensions.ZTypeExtensionInstance

internal class ViewModuleNodeParentEP : ZTypeExtensionInstance<AbstractModuleNode>() {
  @Attribute("moduleNodeClass")
  lateinit var moduleNodeClass: String

  override fun getClassName(): String = moduleNodeClass

  fun getModuleNodeClass(): Class<AbstractModuleNode> {
    return findClass()
  }
}

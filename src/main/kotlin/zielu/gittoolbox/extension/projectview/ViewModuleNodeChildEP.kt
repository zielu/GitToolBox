package zielu.gittoolbox.extension.projectview

import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.util.xmlb.annotations.Attribute
import zielu.intellij.extensions.ZTypeExtensionInstance

internal class ViewModuleNodeChildEP : ZTypeExtensionInstance<ProjectViewNode<*>>() {
  @Attribute("nodeClass")
  lateinit var nodeClass: String

  override fun getClassName(): String = nodeClass

  fun getNodeClass(): Class<ProjectViewNode<*>> {
    return findClass()
  }
}

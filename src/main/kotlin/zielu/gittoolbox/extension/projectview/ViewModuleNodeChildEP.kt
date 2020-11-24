package zielu.gittoolbox.extension.projectview

import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.openapi.extensions.AbstractExtensionPointBean
import com.intellij.util.xmlb.annotations.Attribute

internal class ViewModuleNodeChildEP : AbstractExtensionPointBean() {
  @Attribute("nodeClass")
  lateinit var nodeClass: String

  fun getNodeClass(): Class<ProjectViewNode<*>> {
    return findClassNoExceptions(nodeClass)!!
  }
}

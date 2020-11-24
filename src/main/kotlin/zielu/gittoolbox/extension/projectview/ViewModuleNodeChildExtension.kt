package zielu.gittoolbox.extension.projectview

import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.openapi.extensions.ExtensionPointName
import zielu.intellij.java.toSet

internal class ViewModuleNodeChildExtension {
  fun hasChildOfType(nodeType: Class<in ProjectViewNode<*>>): Boolean {
    val nodeClasses = getNodeClasses()
    return nodeClasses.contains(nodeType)
  }

  private fun getNodeClasses(): Set<Class<in ProjectViewNode<*>>> {
    return EXTENSION_POINT_NAME.extensionList.stream()
      .map { it.getNodeClass() }
      .toSet()
  }
}

private val EXTENSION_POINT_NAME: ExtensionPointName<ViewModuleNodeChildEP> = ExtensionPointName.create(
  "zielu.gittoolbox.viewModuleNodeChild"
)

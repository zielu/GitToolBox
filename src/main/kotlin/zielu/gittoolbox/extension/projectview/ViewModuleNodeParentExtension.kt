package zielu.gittoolbox.extension.projectview

import com.intellij.ide.projectView.impl.nodes.AbstractModuleNode
import com.intellij.openapi.extensions.ExtensionPointName
import zielu.intellij.java.toSet

internal class ViewModuleNodeParentExtension {
  fun hasParentOfType(nodeType: Class<in AbstractModuleNode>): Boolean {
    val moduleNodeClasses = getModuleNodeClasses()
    return moduleNodeClasses.contains(nodeType)
  }

  private fun getModuleNodeClasses(): Set<Class<in AbstractModuleNode>> {
    return EXTENSION_POINT_NAME.extensionList.stream()
      .map { it.getModuleNodeClass() }
      .toSet()
  }
}

private val EXTENSION_POINT_NAME: ExtensionPointName<ViewModuleNodeParentEP> = ExtensionPointName.create(
  "zielu.gittoolbox.viewModuleNodeParent"
)

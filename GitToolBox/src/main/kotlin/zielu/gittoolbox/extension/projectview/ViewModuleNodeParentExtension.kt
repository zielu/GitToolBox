package zielu.gittoolbox.extension.projectview

import com.intellij.ide.projectView.impl.nodes.AbstractModuleNode
import com.intellij.openapi.extensions.ExtensionPointName

internal class ViewModuleNodeParentExtension {
  fun getModuleNodeClasses(): Set<Class<in AbstractModuleNode>> {
    return EXTENSION_POINT_NAME.extensionList.asSequence()
      .map { it.getModuleNodeClass() }
      .toSet()
  }
}

private val EXTENSION_POINT_NAME: ExtensionPointName<ViewModuleNodeParentEP> = ExtensionPointName.create(
  "zielu.gittoolbox.viewModuleNodeParent")

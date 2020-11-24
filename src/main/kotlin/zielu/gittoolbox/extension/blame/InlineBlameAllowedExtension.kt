package zielu.gittoolbox.extension.blame

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import zielu.gittoolbox.util.AppUtil
import java.util.stream.Stream

internal class InlineBlameAllowedExtension(private val project: Project) {
  fun isBlameAllowed(): Boolean {
    return extensions().allMatch { ext -> ext.isAllowed(project) }
  }

  private fun extensions(): Stream<InlineBlameAllowed> {
    return EXTENSION_POINT_NAME.extensionList
      .stream()
      .map { ext -> ext.instantiate() }
  }

  companion object {
    fun isBlameAllowed(project: Project): Boolean {
      return AppUtil.getServiceInstance(project, InlineBlameAllowedExtension::class.java).isBlameAllowed()
    }
  }
}

private val EXTENSION_POINT_NAME: ExtensionPointName<InlineBlameAllowedEP> = ExtensionPointName.create(
  "zielu.gittoolbox.inlineBlameAllowed"
)

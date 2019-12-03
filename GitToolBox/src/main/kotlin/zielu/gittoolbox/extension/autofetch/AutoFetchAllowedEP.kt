package zielu.gittoolbox.extension.autofetch

import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import zielu.intellij.extensions.ZAbstractExtensionPointBean

internal class AutoFetchAllowedEP : ZAbstractExtensionPointBean() {
  @Attribute("provider")
  lateinit var provider: String

  fun instantiate(project: Project): AutoFetchAllowed {
    return createInstance(provider, project)
  }
}

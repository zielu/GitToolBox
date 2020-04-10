package zielu.gittoolbox.extension.projectview

import com.intellij.util.xmlb.annotations.Attribute
import zielu.intellij.extensions.ZAbstractExtensionPointBean

internal class ViewPsiDirectoryNodeEP : ZAbstractExtensionPointBean() {
  @Attribute("finder")
  lateinit var finder: String

  fun instantiate(): PsiDirectoryNodeRepoFinder {
    return createInstance(finder)
  }
}

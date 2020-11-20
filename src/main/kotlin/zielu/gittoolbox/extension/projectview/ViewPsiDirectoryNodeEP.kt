package zielu.gittoolbox.extension.projectview

import com.intellij.util.xmlb.annotations.Attribute
import zielu.intellij.extensions.ZAbstractLazyExtensionPoint

internal class ViewPsiDirectoryNodeEP : ZAbstractLazyExtensionPoint<PsiDirectoryNodeRepoFinder>() {
  @Attribute("finder")
  lateinit var finder: String

  override fun getImplementationClassName() = finder

  fun instantiate(): PsiDirectoryNodeRepoFinder {
    return createInstance()
  }
}

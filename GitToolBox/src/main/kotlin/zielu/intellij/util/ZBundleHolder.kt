package zielu.intellij.util

import java.util.ResourceBundle

class ZBundleHolder(private val bundleName: String) {
  val bundle: ResourceBundle by lazy {
    ResourceBundle.getBundle(bundleName)
  }
}

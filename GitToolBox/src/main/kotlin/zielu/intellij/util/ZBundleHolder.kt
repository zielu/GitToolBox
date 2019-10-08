package zielu.intellij.util

import java.util.ResourceBundle

class ZBundleHolder(private val bundleName: String) {
  private var bundle: ResourceBundle? = null

  fun getBundle(): ResourceBundle {
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(bundleName)
    }
    return checkNotNull(bundle) { "Failed to load resource bundle $bundleName" }
  }
}

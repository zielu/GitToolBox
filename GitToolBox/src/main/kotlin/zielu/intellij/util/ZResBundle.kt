package zielu.intellij.util

import com.intellij.BundleBase
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

internal object ZResBundle {
  @NonNls
  private const val BUNDLE_NAME = "zielu.intellij.ZResBundle"
  private val BUNDLE_HOLDER = ZBundleHolder(BUNDLE_NAME)

  fun message(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String, vararg params: Any?): String {
    return BundleBase.message(BUNDLE_HOLDER.bundle, key, *params)
  }
}

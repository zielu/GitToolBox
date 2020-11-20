package zielu.intellij.util

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

internal object ZResBundle : DynamicBundle(BUNDLE_NAME) {

  fun message(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String, vararg params: Any?): String {
    return getMessage(key, *params)
  }
}

@NonNls
private const val BUNDLE_NAME = "zielu.intellij.ZResBundle"

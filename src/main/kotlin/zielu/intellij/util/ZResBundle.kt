package zielu.intellij.util

import com.intellij.AbstractBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

internal object ZResBundle : AbstractBundle(BUNDLE_NAME) {

  fun message(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String, vararg params: Any?): String {
    return getMessage(key, *params)
  }

  fun na(): String {
    return message("generic.na")
  }
}

@NonNls
private const val BUNDLE_NAME = "zielu.intellij.ZResBundle"

package zielu.gittoolbox

import com.intellij.AbstractBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

internal object ResBundle : AbstractBundle(BUNDLE_NAME) {
  @JvmStatic
  fun message(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String, vararg params: Any?): String {
    return getMessage(key, *params)
  }

  @JvmStatic
  fun on(): String {
    return message("common.on")
  }

  @JvmStatic
  fun disabled(): String {
    return message("common.disabled")
  }
}

@NonNls
private const val BUNDLE_NAME = "zielu.gittoolbox.ResourceBundle"

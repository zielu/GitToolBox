package zielu.gittoolbox

import com.intellij.AbstractBundle
import org.jetbrains.annotations.PropertyKey
import zielu.intellij.util.ZBundleHolder

internal object ResBundle {
  private const val bundleName = "zielu.gittoolbox.ResourceBundle"
  private val holder = ZBundleHolder(bundleName)

  @JvmStatic
  fun message(@PropertyKey(resourceBundle = bundleName) key: String, vararg params: Any?): String {
    return AbstractBundle.message(holder.bundle, key, *params)
  }

  @JvmStatic
  fun na(): String {
    return message("common.na")
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

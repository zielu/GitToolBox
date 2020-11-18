package zielu.gittoolbox.completion.gitmoji

import com.intellij.AbstractBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import zielu.intellij.util.ZBundleHolder

internal object GitmojiResBundle {
  @NonNls
  private const val BUNDLE_NAME = "zielu.gittoolbox.gitmoji"
  private val BUNDLE_HOLDER = ZBundleHolder(BUNDLE_NAME)

  fun message(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String, vararg params: Any?): String {
    return AbstractBundle.message(BUNDLE_HOLDER.bundle, key, *params)
  }

  fun keySet(): Set<String> {
    return BUNDLE_HOLDER.bundle.keySet()
  }
}

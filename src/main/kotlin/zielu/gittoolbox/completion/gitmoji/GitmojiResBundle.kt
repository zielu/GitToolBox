package zielu.gittoolbox.completion.gitmoji

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

internal object GitmojiResBundle {
  @NonNls
  private const val BUNDLE_NAME = "zielu.gittoolbox.gitmoji"
  private val bundle = DynamicBundle(BUNDLE_NAME)

  fun message(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String, vararg params: Any?): String {
    return bundle.getMessage(key, *params)
  }

  fun keySet(): Set<String> {
    return bundle.resourceBundle.keySet()
  }
}

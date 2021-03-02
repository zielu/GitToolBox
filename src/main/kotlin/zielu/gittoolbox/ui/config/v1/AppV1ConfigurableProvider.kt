package zielu.gittoolbox.ui.config.v1

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import zielu.gittoolbox.GitToolBoxRegistry
import zielu.gittoolbox.ui.config.app.GtConfigurable

internal class AppV1ConfigurableProvider : ConfigurableProvider() {
  override fun createConfigurable(): Configurable {
    return GtConfigurable()
  }

  override fun canCreateConfigurable(): Boolean {
    return GitToolBoxRegistry.useLegacyConfig()
  }
}

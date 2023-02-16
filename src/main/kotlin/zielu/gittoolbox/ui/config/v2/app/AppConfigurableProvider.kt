package zielu.gittoolbox.ui.config.v2.app

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import zielu.gittoolbox.GitToolBoxRegistry

internal class AppConfigurableProvider : ConfigurableProvider() {
  override fun createConfigurable(): Configurable {
    return AppConfigurable()
  }

  override fun canCreateConfigurable(): Boolean {
    return GitToolBoxRegistry.useNewConfig()
  }
}

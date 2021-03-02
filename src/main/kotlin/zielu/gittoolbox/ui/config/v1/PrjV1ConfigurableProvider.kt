package zielu.gittoolbox.ui.config.v1

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import zielu.gittoolbox.GitToolBoxRegistry
import zielu.gittoolbox.ui.config.prj.GtProjectConfigurable

internal class PrjV1ConfigurableProvider(private val project: Project) : ConfigurableProvider() {
  override fun createConfigurable(): Configurable {
    return GtProjectConfigurable(project)
  }

  override fun canCreateConfigurable(): Boolean {
    return GitToolBoxRegistry.useLegacyConfig()
  }
}

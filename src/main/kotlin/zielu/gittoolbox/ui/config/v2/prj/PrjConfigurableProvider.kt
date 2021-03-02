package zielu.gittoolbox.ui.config.v2.prj

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import zielu.gittoolbox.GitToolBoxRegistry

internal class PrjConfigurableProvider(private val project: Project) : ConfigurableProvider() {
  override fun createConfigurable(): Configurable {
    return PrjConfigurable(project)
  }

  override fun canCreateConfigurable(): Boolean {
    return GitToolBoxRegistry.useNewConfig()
  }
}

package zielu.gittoolbox.ui.config.v2.prj

import com.intellij.openapi.project.Project
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.config.ProjectConfig
import zielu.intellij.ui.GtConfigurableBase

internal class PrjConfigurable(private val project: Project) : GtConfigurableBase<PrjConfigForm, MutableConfig>() {
  override fun getDisplayName(): String {
    return ResBundle.message("configurable.prj.displayName")
  }

  override fun createForm(): PrjConfigForm {
    return PrjConfigForm()
  }

  override fun getConfig(): MutableConfig {
    return MutableConfig(AppConfig.getConfig(), ProjectConfig.getConfig(project), project)
  }

  override fun setFormState(form: PrjConfigForm, config: MutableConfig) {
    form.fillFromState(config)
  }

  override fun checkModified(form: PrjConfigForm, config: MutableConfig): Boolean {
    return form.isModified()
  }

  override fun doApply(form: PrjConfigForm, config: MutableConfig) {
    form.applyToState(config)
  }

  override fun afterApply(before: MutableConfig, updated: MutableConfig) {
    ProjectConfig.getInstance(project).stateUpdated(before.prj())
  }

  override fun copyConfig(config: MutableConfig): MutableConfig {
    return config.copy()
  }
}

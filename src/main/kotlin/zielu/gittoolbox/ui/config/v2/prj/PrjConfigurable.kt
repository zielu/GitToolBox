package zielu.gittoolbox.ui.config.v2.prj

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.config.ProjectConfig
import zielu.intellij.ui.GtConfigurableBase

internal class PrjConfigurable(private val project: Project) : GtConfigurableBase<PrjConfigForm, MutableConfig>() {
  override fun getDisplayName(): String {
    return "Git Toolbox Project V2"
  }

  override fun createForm(): PrjConfigForm {
    val form = PrjConfigForm()
    Disposer.register(this, form)
    return form
  }

  override fun getConfig(): MutableConfig {
    return MutableConfig(AppConfig.getConfig(), ProjectConfig.getConfig(project))
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
}

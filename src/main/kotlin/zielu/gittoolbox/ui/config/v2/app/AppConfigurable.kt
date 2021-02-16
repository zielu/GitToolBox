package zielu.gittoolbox.ui.config.v2.app

import com.intellij.openapi.util.Disposer
import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.config.MutableConfig
import zielu.intellij.ui.GtConfigurableBase

internal class AppConfigurable : GtConfigurableBase<AppConfigForm, MutableConfig>() {
  override fun createForm(): AppConfigForm {
    val form = AppConfigForm()
    Disposer.register(this, form)
    return form
  }

  override fun getDisplayName(): String {
    return "GitToolbox V2"
  }

  override fun setFormState(form: AppConfigForm, config: MutableConfig) {
    form.fillFromState(config)
  }

  override fun getConfig(): MutableConfig {
    return MutableConfig(AppConfig.getConfig())
  }

  override fun checkModified(form: AppConfigForm, config: MutableConfig): Boolean {
    return form.isModified()
  }

  override fun doApply(form: AppConfigForm, config: MutableConfig) {
    form.applyToState(config)
  }
}

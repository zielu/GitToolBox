package zielu.gittoolbox.ui.config.v2.app

import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.config.MutableConfig
import zielu.intellij.ui.GtConfigurableBase

internal class AppConfigurable : GtConfigurableBase<AppConfigForm, MutableConfig>() {
  override fun createForm(): AppConfigForm {
    return AppConfigForm()
  }

  override fun getDisplayName(): String {
    return ResBundle.message("configurable.app.displayName")
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

  override fun afterApply(before: MutableConfig, updated: MutableConfig) {
    AppConfig.getInstance().stateUpdated(before.app)
  }

  override fun copyConfig(config: MutableConfig): MutableConfig {
    return config.copy()
  }
}

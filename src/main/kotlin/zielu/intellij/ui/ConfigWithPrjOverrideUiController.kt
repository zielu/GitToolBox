package zielu.intellij.ui

import zielu.intellij.util.ZProperty

internal class ConfigWithPrjOverrideUiController<T> {
  private lateinit var overrideConfig: ZProperty<Boolean>
  private lateinit var overrideUi: ZProperty<Boolean>
  private lateinit var configValue: ZProperty<T>
  private lateinit var prjConfigValue: ZProperty<T>
  private lateinit var prjUi: ZProperty<T>
  private lateinit var prjUiEnabled: ZProperty<Boolean>

  fun bindOverrideEnabled(config: ZProperty<Boolean>, ui: ZProperty<Boolean>) {
    overrideConfig = config
    overrideUi = ui
  }

  fun bindValue(
    configValue: ZProperty<T>,
    prjConfigValue: ZProperty<T>,
    prjUi: ZProperty<T>,
    prjUiEnabled: ZProperty<Boolean>
  ) {
    this.configValue = configValue
    this.prjConfigValue = prjConfigValue
    this.prjUi = prjUi
    this.prjUiEnabled = prjUiEnabled
  }

  fun populateUi() {
    val overrideEnabled = overrideConfig.get()
    overrideUi.set(overrideEnabled)
    if (overrideEnabled) {
      prjUi.set(prjConfigValue.get())
    } else {
      if (prjConfigValue.get() == null) {
        prjUi.set(configValue.get())
      } else {
        prjUi.set(prjConfigValue.get())
      }
    }
    prjUiEnabled.set(overrideEnabled)
  }

  fun populateConfig() {
    val overrideEnabled = overrideUi.get()
    overrideConfig.set(overrideEnabled)
    if (overrideEnabled) {
      prjConfigValue.set(prjUi.get())
    }
  }
}

package zielu.gittoolbox.ui.config.v2.app

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.selected
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.fetch.AutoFetchParams
import zielu.intellij.ui.GtFormUiEx
import zielu.intellij.util.createIntProperty
import javax.swing.JComponent

internal class AutoFetchPage : GtFormUiEx<MutableConfig> {
  private val autoFetchEnabled = AtomicBooleanProperty(true)
  private val autoFetchInterval = createIntProperty(AutoFetchParams.DEFAULT_INTERVAL_MINUTES)
  private val autoFetchOnBranchSwitch = AtomicBooleanProperty(true)

  private val exclusionsForm = AutoFetchExclusionsForm()
  private lateinit var panel: DialogPanel
  private lateinit var exclusionsRow: Row

  private lateinit var state: MutableConfig

  override fun init() {
    panel = panel {
      row {
        val autoFetchCheckBox = checkBox(
          ResBundle.message("configurable.app.autoFetchEnabled.label"),
          autoFetchEnabled::get,
          autoFetchEnabled::set
        )
        cell {
          spinner(autoFetchInterval::value, AutoFetchParams.INTERVAL_MIN_MINUTES, AutoFetchParams.INTERVAL_MAX_MINUTES)
            .enableIf(autoFetchCheckBox.selected)
          label(ResBundle.message("configurable.app.autoFetchUnits.label"))
        }
      }
      row {
        checkBox(
          ResBundle.message("configurable.app.autoFetchOnBranchSwitchEnabled.label"),
          autoFetchOnBranchSwitch::get,
          autoFetchOnBranchSwitch::set
        )
      }
      exclusionsRow = row {
        exclusionsForm.init()
        val content = exclusionsForm.content
        content()
      }
    }
    exclusionsRow.visible = false
  }

  override fun fillFromState(state: MutableConfig) {
    this.state = state

    autoFetchEnabled.set(state.app.autoFetchEnabled) // TODO:
    autoFetchInterval.value = state.app.autoFetchIntervalMinutes // TODO:
    autoFetchOnBranchSwitch.set(state.app.autoFetchOnBranchSwitch) // TODO:

    exclusionsForm.fillFromState(state)
  }

  override fun isModified(): Boolean {
    return panel.isModified() || exclusionsForm.isModified()
  }

  override fun getContent(): JComponent {
    return panel
  }

  override fun afterStateSet() {
    panel.reset()
    if (state.hasProject()) {
      exclusionsRow.visible = true
      exclusionsForm.afterStateSet()
    }
  }

  override fun applyToState(state: MutableConfig) {
    panel.apply()
    state.app.autoFetchEnabled = autoFetchEnabled.get()

    exclusionsForm.applyToState(state)
  }
}

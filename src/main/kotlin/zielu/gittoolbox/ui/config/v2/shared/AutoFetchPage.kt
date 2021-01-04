package zielu.gittoolbox.ui.config.v2.shared

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.selected
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.fetch.AutoFetchParams
import zielu.gittoolbox.ui.config.v2.BoolProp
import zielu.gittoolbox.ui.config.v2.BoolPropWithOverride
import zielu.gittoolbox.ui.config.v2.IntPropWithOverride
import zielu.gittoolbox.ui.config.v2.UiItem
import zielu.gittoolbox.ui.config.v2.ValueProp
import zielu.intellij.ui.GtFormUiEx
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JPanel

internal class AutoFetchPage : GtFormUiEx<MutableConfig> {
  private val autoFetchEnabled = AtomicBooleanProperty(true)
  private val autoFetchInterval = AtomicLazyProperty {
    AutoFetchParams.DEFAULT_INTERVAL_MINUTES
  }
  private val autoFetchOverride = AtomicBooleanProperty(false)

  private val autoFetchOnBranchSwitch = AtomicBooleanProperty(true)

  private val exclusionsForm = AutoFetchExclusionsForm()

  private val uiItems = mutableListOf<UiItem>()

  private var hasProject: Boolean = false

  private lateinit var panel: DialogPanel

  private lateinit var autoFetchTimingOverrideCheckbox: JBCheckBox

  override fun init() {
    exclusionsForm.init()
    val exclusionsPanel = JPanel(GridLayout(1, 1, 5, 0))
    exclusionsPanel.add(exclusionsForm.content)

    panel = panel {
      row {
        val autoFetchCheckBox = checkBox(
          ResBundle.message("configurable.app.autoFetchEnabled.label"),
          autoFetchEnabled::get,
          autoFetchEnabled::set
        )
        cell {
          spinner(
            autoFetchInterval::get,
            autoFetchInterval::set,
            AutoFetchParams.INTERVAL_MIN_MINUTES,
            AutoFetchParams.INTERVAL_MAX_MINUTES
          ).enableIf(autoFetchCheckBox.selected)
          label(ResBundle.message("configurable.app.autoFetchUnits.label"))
        }
        right {
          autoFetchTimingOverrideCheckbox = checkBox(
            ResBundle.message("common.override"),
            autoFetchOverride::get,
            autoFetchOverride::set
          ).component
        }
      }
      row {
        checkBox(
          ResBundle.message("configurable.app.autoFetchOnBranchSwitchEnabled.label"),
          autoFetchOnBranchSwitch::get,
          autoFetchOnBranchSwitch::set
        )
      }
      row {
        exclusionsPanel(CCFlags.growX)
      }
    }
    autoFetchTimingOverrideCheckbox.isVisible = false
  }

  override fun fillFromState(state: MutableConfig) {
    hasProject = state.hasProject()

    if (hasProject) {
      uiItems.add(
        BoolPropWithOverride(
          autoFetchEnabled,
          autoFetchOverride,
          state.app::autoFetchEnabled,
          state.prj().autoFetchEnabledOverride
        )
      )
      uiItems.add(
        IntPropWithOverride(
          autoFetchInterval,
          autoFetchOverride,
          state.app::autoFetchIntervalMinutes,
          state.prj().autoFetchIntervalMinutesOverride
        )
      )
    } else {
      uiItems.add(BoolProp(autoFetchEnabled, state.app::autoFetchEnabled))
      uiItems.add(ValueProp(autoFetchInterval, state.app::autoFetchIntervalMinutes))
      autoFetchEnabled.set(state.app.autoFetchEnabled)
    }
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
    exclusionsForm.setVisible(hasProject)
    if (hasProject) {
      exclusionsForm.afterStateSet()
      autoFetchTimingOverrideCheckbox.isVisible = true
    }
  }

  override fun applyToState(state: MutableConfig) {
    panel.apply()
    exclusionsForm.applyToState(state)
    uiItems.forEach(UiItem::apply)
  }
}

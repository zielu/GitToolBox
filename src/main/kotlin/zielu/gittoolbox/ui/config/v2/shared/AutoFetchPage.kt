package zielu.gittoolbox.ui.config.v2.shared

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
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
  private val autoFetchOnBranchSwitchOverride = AtomicBooleanProperty(false)
  private val exclusionsForm = AutoFetchExclusionsForm()
  private val uiItems = mutableListOf<UiItem>()
  private var hasProject: Boolean = false
  private lateinit var panel: DialogPanel
  private lateinit var autoFetchCheckBox: JBCheckBox
  private lateinit var autoFetchIntervalSpinner: JBIntSpinner
  private lateinit var autoFetchTimingOverrideCheckbox: JBCheckBox
  private lateinit var autoFetchOnBranchSwitchCheckbox: JBCheckBox
  private lateinit var autoFetchOnBranchSwitchOverrideCheckbox: JBCheckBox

  override fun init() {
    exclusionsForm.init()
    val exclusionsPanel = JPanel(GridLayout(1, 1, 5, 0))
    exclusionsPanel.add(exclusionsForm.content)

    panel = panel {
      row {
        autoFetchCheckBox = checkBox(
          ResBundle.message("configurable.app.autoFetchEnabled.label"),
          autoFetchEnabled::get,
          autoFetchEnabled::set
        ).component
        cell {
          autoFetchIntervalSpinner = spinner(
            autoFetchInterval::get,
            autoFetchInterval::set,
            AutoFetchParams.INTERVAL_MIN_MINUTES,
            AutoFetchParams.INTERVAL_MAX_MINUTES
          ).component
          label(ResBundle.message("configurable.app.autoFetchUnits.label"))
        }
        right {
          autoFetchTimingOverrideCheckbox = checkBox(
            ResBundle.message("common.override"),
            autoFetchOverride::get,
            autoFetchOverride::set
          ).component
          autoFetchTimingOverrideCheckbox.toolTipText = ResBundle.message("common.override.tooltip")
        }
      }
      row {
        autoFetchOnBranchSwitchCheckbox = checkBox(
          ResBundle.message("configurable.app.autoFetchOnBranchSwitchEnabled.label"),
          autoFetchOnBranchSwitch::get,
          autoFetchOnBranchSwitch::set
        ).component
        right {
          autoFetchOnBranchSwitchOverrideCheckbox = checkBox(
            ResBundle.message("common.override"),
            autoFetchOnBranchSwitchOverride::get,
            autoFetchOnBranchSwitchOverride::set

          ).component
          autoFetchTimingOverrideCheckbox.toolTipText = ResBundle.message("common.override.tooltip")
        }
      }
      row {
        exclusionsPanel(CCFlags.growX)
      }
    }
    autoFetchTimingOverrideCheckbox.isVisible = false
    autoFetchOnBranchSwitchOverrideCheckbox.isVisible = false

    autoFetchCheckBox.addItemListener { updateTimingUi() }
    autoFetchTimingOverrideCheckbox.addItemListener { updateTimingOverrideUi() }
    autoFetchOnBranchSwitchOverrideCheckbox.addItemListener { updateOnBranchSwitchOverrideUi() }
  }

  override fun fillFromState(state: MutableConfig) {
    hasProject = state.hasProject()
    uiItems.clear()

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
      uiItems.add(
        BoolPropWithOverride(
          autoFetchOnBranchSwitch,
          autoFetchOnBranchSwitchOverride,
          state.app::autoFetchOnBranchSwitch,
          state.prj().autoFetchOnBranchSwitchOverride
        )
      )

      updateTimingUi()
      updateTimingOverrideUi()
      updateOnBranchSwitchOverrideUi()
    } else {
      uiItems.add(BoolProp(autoFetchEnabled, state.app::autoFetchEnabled))
      uiItems.add(ValueProp(autoFetchInterval, state.app::autoFetchIntervalMinutes))
      autoFetchEnabled.set(state.app.autoFetchEnabled)
      autoFetchOnBranchSwitch.set(state.app.autoFetchOnBranchSwitch)
      updateTimingUi()
    }

    exclusionsForm.fillFromState(state)
  }

  private fun updateTimingOverrideUi() {
    val override = autoFetchTimingOverrideCheckbox.isSelected
    autoFetchCheckBox.isEnabled = override
    if (override) {
      updateTimingUi()
    } else {
      autoFetchIntervalSpinner.isEnabled = false
    }
  }

  private fun updateTimingUi() {
    autoFetchIntervalSpinner.isEnabled = autoFetchCheckBox.isSelected
  }

  private fun updateOnBranchSwitchOverrideUi() {
    autoFetchOnBranchSwitchCheckbox.isEnabled = autoFetchOnBranchSwitchOverrideCheckbox.isSelected
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
      autoFetchOnBranchSwitchOverrideCheckbox.isVisible = true
    }
  }

  override fun applyToState(state: MutableConfig) {
    panel.apply()
    exclusionsForm.applyToState(state)
    uiItems.forEach(UiItem::apply)
  }
}

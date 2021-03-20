package zielu.gittoolbox.ui.config.v2.shared

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.fetch.AutoFetchParams
import zielu.gittoolbox.ui.config.v2.props.BoolProp
import zielu.gittoolbox.ui.config.v2.props.BoolPropWithOverride
import zielu.gittoolbox.ui.config.v2.props.IntPropWithOverride
import zielu.gittoolbox.ui.config.v2.props.UiItems
import zielu.gittoolbox.ui.config.v2.props.ValueProp
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
  private val uiItems = UiItems()
  private var hasProject: Boolean = false
  private val overrideCheckBoxes = OverrideCheckBoxes()
  private lateinit var panel: DialogPanel
  private lateinit var autoFetchCheckBox: JBCheckBox
  private lateinit var autoFetchIntervalSpinner: JBIntSpinner
  private lateinit var autoFetchTimingOverrideCheckbox: JBCheckBox
  private lateinit var autoFetchOnBranchSwitchCheckbox: JBCheckBox
  private lateinit var autoFetchOnBranchSwitchOverrideCheckbox: JBCheckBox

  init {
    Disposer.register(this, uiItems)
  }

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
          overrideCheckBoxes.register(autoFetchTimingOverrideCheckbox)
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
          overrideCheckBoxes.register(autoFetchTimingOverrideCheckbox)
        }
      }
      row {
        exclusionsPanel(CCFlags.growX)
      }
    }
    overrideCheckBoxes.hide()

    autoFetchCheckBox.addItemListener { updateTimingUi() }
    autoFetchTimingOverrideCheckbox.addItemListener { updateTimingOverrideUi() }
    autoFetchOnBranchSwitchOverrideCheckbox.addItemListener { updateOnBranchSwitchOverrideUi() }
  }

  override fun fillFromState(state: MutableConfig) {
    hasProject = state.hasProject()
    uiItems.clear()

    if (hasProject) {
      uiItems.register(
        BoolPropWithOverride(
          autoFetchEnabled,
          autoFetchOverride,
          state.app::autoFetchEnabled,
          state.prj().autoFetchEnabledOverride,
          autoFetchCheckBox::setSelected,
          autoFetchTimingOverrideCheckbox
        ),
        IntPropWithOverride(
          autoFetchInterval,
          autoFetchOverride,
          state.app::autoFetchIntervalMinutes,
          state.prj().autoFetchIntervalMinutesOverride,
          autoFetchIntervalSpinner::setValue,
          autoFetchTimingOverrideCheckbox
        ),
        BoolPropWithOverride(
          autoFetchOnBranchSwitch,
          autoFetchOnBranchSwitchOverride,
          state.app::autoFetchOnBranchSwitch,
          state.prj().autoFetchOnBranchSwitchOverride,
          autoFetchOnBranchSwitchCheckbox::setSelected,
          autoFetchOnBranchSwitchOverrideCheckbox
        )
      )

      updateTimingUi()
      updateTimingOverrideUi()
      updateOnBranchSwitchOverrideUi()
    } else {
      uiItems.register(
        BoolProp(autoFetchEnabled, state.app::autoFetchEnabled),
        ValueProp(autoFetchInterval, state.app::autoFetchIntervalMinutes),
        BoolProp(autoFetchOnBranchSwitch, state.app::autoFetchOnBranchSwitch)
      )
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
    return panel.isModified() || exclusionsForm.isModified() || uiItems.isModified()
  }

  override fun getContent(): JComponent {
    return panel
  }

  override fun afterStateSet() {
    panel.reset()
    exclusionsForm.setVisible(hasProject)
    if (hasProject) {
      exclusionsForm.afterStateSet()
      overrideCheckBoxes.show()
    }
  }

  override fun applyToState(state: MutableConfig) {
    panel.apply()
    exclusionsForm.applyToState(state)
    uiItems.apply()
  }
}

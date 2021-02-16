package zielu.gittoolbox.ui.config.v2.shared

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.selectedValueMatches
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.config.ReferencePointForStatusType
import zielu.gittoolbox.ui.config.ReferencePointForStatusTypeRenderer
import zielu.gittoolbox.ui.config.v2.props.ValuePropWithOverride
import zielu.gittoolbox.ui.config.v2.props.UiItems
import zielu.gittoolbox.ui.config.v2.props.ValueProp
import zielu.intellij.ui.GtFormUiEx
import javax.swing.JComponent

internal class StatusPage : GtFormUiEx<MutableConfig> {
  private val referencePointForStatus = AtomicLazyProperty {
    ReferencePointForStatusType.AUTOMATIC
  }
  private val referencePointName = AtomicLazyProperty {
    ""
  }
  private val referencePointOverride = AtomicBooleanProperty(false)
  private lateinit var referencePointComboBox: ComboBox<ReferencePointForStatusType>
  private lateinit var referencePointNameTextField: JBTextField
  private lateinit var referencePointOverrideCheckBox: JBCheckBox

  private lateinit var panel: DialogPanel

  private val uiItems = UiItems()
  private val overrideCheckBoxes = OverrideCheckBoxes()
  private var hasProject = false

  init {
    Disposer.register(this, uiItems)
  }

  override fun init() {
    panel = panel {
      row {
        label(ResBundle.message("configurable.prj.parentBranch.label"))
        cell {
          referencePointComboBox = comboBox(
            CollectionComboBoxModel(ReferencePointForStatusType.allValues()),
            referencePointForStatus::get,
            { referencePointForStatus.set(it!!) },
            ReferencePointForStatusTypeRenderer()
          ).component
          referencePointNameTextField = textField(
            referencePointName::get,
            { referencePointName.set(it) }
          ).component
        }
        right {
          referencePointOverrideCheckBox = checkBox(
            ResBundle.message("common.override"),
            referencePointOverride::get,
            referencePointOverride::set
          ).component
          overrideCheckBoxes.register(referencePointOverrideCheckBox)
        }
      }
    }
    overrideCheckBoxes.hide()

    referencePointOverrideCheckBox.addItemListener { updateOverrideUi() }
    referencePointComboBox.addItemListener { updateUi() }
  }

  private fun updateUi() {
    referencePointNameTextField.isEnabled = referencePointComboBox.selectedValueMatches {
      ReferencePointForStatusType.SELECTED_PARENT_BRANCH == it
    }.invoke()
  }

  private fun updateOverrideUi() {
    val override = referencePointOverrideCheckBox.isSelected
    referencePointComboBox.isEnabled = override
    if (override) {
      updateUi()
    } else {
      referencePointNameTextField.isEnabled = false
    }
  }

  override fun fillFromState(state: MutableConfig) {
    hasProject = state.hasProject()
    uiItems.clear()

    if (hasProject) {
      uiItems.register(
        ValuePropWithOverride(
          referencePointForStatus,
          referencePointOverride,
          state.app.referencePointForStatus::type,
          state.prj().referencePointForStatusOverride::enabled,
          state.prj().referencePointForStatusOverride.value::type
        ),
        ValuePropWithOverride(
          referencePointName,
          referencePointOverride,
          state.app.referencePointForStatus::name,
          state.prj().referencePointForStatusOverride::enabled,
          state.prj().referencePointForStatusOverride.value::name
        )
      )
      updateUi()
      updateOverrideUi()
    } else {
      uiItems.register(
        ValueProp(
          referencePointForStatus,
          state.app.referencePointForStatus::type
        ),
        ValueProp(
          referencePointName,
          state.app.referencePointForStatus::name
        )
      )
      updateUi()
    }
  }

  override fun isModified(): Boolean {
    return panel.isModified()
  }

  override fun getContent(): JComponent {
    return panel
  }

  override fun afterStateSet() {
    panel.reset()
    if (hasProject) {
      overrideCheckBoxes.show()
    }
  }

  override fun applyToState(state: MutableConfig) {
    panel.apply()
    uiItems.apply()
  }
}

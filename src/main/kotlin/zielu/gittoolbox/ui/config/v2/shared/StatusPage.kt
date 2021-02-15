package zielu.gittoolbox.ui.config.v2.shared

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.selectedValueMatches
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.config.ReferencePointForStatusType
import zielu.gittoolbox.ui.config.ReferencePointForStatusTypeRenderer
import zielu.gittoolbox.ui.config.v2.RefPropWithOverride
import zielu.gittoolbox.ui.config.v2.UiItem
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

  private val uiItems = mutableListOf<UiItem>()
  private var hasProject = false

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
          referencePointOverrideCheckBox.toolTipText = ResBundle.message("common.override.tooltip")
        }
      }
    }
    referencePointOverrideCheckBox.isVisible = false
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
      uiItems.add(
        RefPropWithOverride(
          referencePointForStatus,
          referencePointOverride,
          state.app.referencePointForStatus::type,
          state.prj().referencePointForStatusOverride::enabled,
          state.prj().referencePointForStatusOverride.value::type
        )
      )
      uiItems.add(
        RefPropWithOverride(
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
      referencePointForStatus.set(state.app.referencePointForStatus.type)
      referencePointName.set(state.app.referencePointForStatus.name)
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
      referencePointOverrideCheckBox.isVisible = true
    }
  }

  override fun applyToState(state: MutableConfig) {
    panel.apply()
    uiItems.forEach(UiItem::apply)
  }
}

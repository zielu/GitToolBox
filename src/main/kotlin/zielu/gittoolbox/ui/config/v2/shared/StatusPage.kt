package zielu.gittoolbox.ui.config.v2.shared

import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.selectedValueMatches
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.config.ReferencePointForStatusType
import zielu.gittoolbox.ui.config.ReferencePointForStatusTypeRenderer
import zielu.intellij.ui.GtFormUiEx
import javax.swing.JComponent

internal class StatusPage : GtFormUiEx<MutableConfig> {
  private val referencePointForStatus = AtomicLazyProperty {
    ReferencePointForStatusType.AUTOMATIC
  }
  private val referencePointName = AtomicLazyProperty {
    ""
  }
  private lateinit var panel: DialogPanel

  override fun init() {
    panel = panel {
      row {
        label(ResBundle.message("configurable.prj.parentBranch.label"))
        cell {
          val referencePointComboBox = comboBox(
            CollectionComboBoxModel(ReferencePointForStatusType.allValues()),
            referencePointForStatus::get,
            { referencePointForStatus.set(it!!) },
            ReferencePointForStatusTypeRenderer()
          )
          textField(
            referencePointName::get,
            { referencePointName.set(it) }
          ).enableIf(
            referencePointComboBox.component.selectedValueMatches {
              ReferencePointForStatusType.SELECTED_PARENT_BRANCH == it
            }
          )
        }
      }
    }
  }

  override fun fillFromState(state: MutableConfig) {
    referencePointForStatus.set(state.app.referencePointForStatus.type)
    referencePointName.set(state.app.referencePointForStatus.name)
  }

  override fun isModified(): Boolean {
    return panel.isModified()
  }

  override fun getContent(): JComponent {
    return panel
  }

  override fun afterStateSet() {
    panel.reset()
  }

  override fun applyToState(state: MutableConfig) {
    panel.apply()
    // TODO: apply to state
  }
}

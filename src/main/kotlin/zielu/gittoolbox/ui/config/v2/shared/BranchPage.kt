package zielu.gittoolbox.ui.config.v2.shared

import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.util.execution.ParametersListUtil
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.ui.config.v2.props.UiItems
import zielu.gittoolbox.ui.config.v2.props.ValueProp
import zielu.intellij.ui.GtFormUiEx
import javax.swing.JComponent

internal class BranchPage : GtFormUiEx<MutableConfig> {
  private val excludedBranches = AtomicLazyProperty {
    ""
  }
  private lateinit var panel: DialogPanel

  private val uiItems = UiItems()
  init {
    Disposer.register(this, uiItems)
  }

  override val content: JComponent
    get() = panel

  override fun init() {
    panel = panel {
      titledRow("Outdated branches cleanup") {
        row("Excluded branches") {
          expandableTextField(
            excludedBranches::get,
            excludedBranches::set,
            ParametersListUtil.COLON_LINE_PARSER,
            ParametersListUtil.COLON_LINE_JOINER
          ).constraints(
            CCFlags.growX
          ).comment(
            "Use ; to separate patterns. Accepted wildcards: <b>?</b> - exactly one symbol; <b>*</b> - zero or more symbols",
            140, true
          )
        }
      }
    }
  }

  override fun fillFromState(state: MutableConfig) {
    uiItems.clear()

    uiItems.register(
      ValueProp(
        excludedBranches,
        { state.app.outdatedBranchesCleanup.exclusionGlobs.joinToString(separator = ";") },
        { state.app.outdatedBranchesCleanup.exclusionGlobs = it.split(';') }
      )
    )
  }

  override fun afterStateSet() {
    panel.reset()
  }

  override fun isModified(): Boolean {
    return panel.isModified() || uiItems.isModified()
  }

  override fun applyToState(state: MutableConfig) {
    panel.apply()
    uiItems.apply()
  }
}

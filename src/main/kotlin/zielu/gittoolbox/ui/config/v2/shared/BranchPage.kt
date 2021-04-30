package zielu.gittoolbox.ui.config.v2.shared

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.util.execution.ParametersListUtil
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.branch.OutdatedBranchCleanupParams
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.ui.config.v2.props.BoolProp
import zielu.gittoolbox.ui.config.v2.props.UiItems
import zielu.gittoolbox.ui.config.v2.props.ValueProp
import zielu.intellij.ui.GtFormUiEx
import javax.swing.JComponent

internal class BranchPage : GtFormUiEx<MutableConfig> {
  private val outdatedAutoCleanupEnabled = AtomicBooleanProperty(false)
  private val outdatedAutoCleanupInterval = AtomicLazyProperty {
    OutdatedBranchCleanupParams.DEFAULT_INTERVAL_HOURS
  }
  private val outdatedCleanupExcludedBranches = AtomicLazyProperty {
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
      titledRow(ResBundle.message("configurable.shared.branchCleanup.section.title")) {
        row {
          checkBox(
            ResBundle.message("configurable.shared.branchCleanup.autoCleanupEnabled.label"),
            outdatedAutoCleanupEnabled::get,
            outdatedAutoCleanupEnabled::set
          )
          cell {
            spinner(
              outdatedAutoCleanupInterval::get,
              outdatedAutoCleanupInterval::set,
              OutdatedBranchCleanupParams.INTERVAL_MIN_HOURS,
              OutdatedBranchCleanupParams.INTERVAL_MAX_HOURS
            )
            label(ResBundle.message("configurable.shared.branchCleanup.autoCleanupUnits.label"))
          }
        }
        row(ResBundle.message("configurable.shared.branchCleanup.exclusions.label")) {
          expandableTextField(
            outdatedCleanupExcludedBranches::get,
            outdatedCleanupExcludedBranches::set,
            ParametersListUtil.COLON_LINE_PARSER,
            ParametersListUtil.COLON_LINE_JOINER
          ).constraints(
            CCFlags.growX
          ).comment(
            ResBundle.message("configurable.shared.branchCleanup.exclusions.comment"),
            140
          )
        }
      }
    }
  }

  override fun fillFromState(state: MutableConfig) {
    uiItems.clear()

    uiItems.register(
      BoolProp(
        outdatedAutoCleanupEnabled,
        state.app.outdatedBranchesCleanup::autoCheckEnabled
      ),
      ValueProp(
        outdatedAutoCleanupInterval,
        state.app.outdatedBranchesCleanup::autoCheckIntervalHours
      ),
      ValueProp(
        outdatedCleanupExcludedBranches,
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

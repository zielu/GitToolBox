package zielu.gittoolbox.ui.config.v2.app

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ex.MultiLineLabel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBLabel
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.AbsoluteDateTimeStyle
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.extension.update.UpdateProjectAction
import zielu.gittoolbox.ui.StatusPresenter
import zielu.gittoolbox.ui.StatusPresenters
import zielu.gittoolbox.ui.config.AbsoluteDateTimeStyleRenderer
import zielu.gittoolbox.ui.config.v2.PresenterPreview
import zielu.gittoolbox.ui.update.UpdateProjectActionService
import zielu.intellij.ui.GtFormUiEx
import java.util.Vector
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.ListCellRenderer

internal class GeneralPage(
  private val appPages: AppPages
) : GtFormUiEx<MutableConfig> {
  private lateinit var panel: DialogPanel
  private val presentationMode = AtomicLazyProperty<StatusPresenter>(StatusPresenters::defaultPresenter)
  private val showStatusWidget = AtomicBooleanProperty(true)
  private val showChangesInStatusBar = AtomicBooleanProperty(true)
  private val showBlameWidget = AtomicBooleanProperty(true)
  private val showEditorInlineBlame = AtomicBooleanProperty(true)
  private val showProjectViewDecoration = AtomicBooleanProperty(true)
  private val behindTracker = AtomicBooleanProperty(true)
  private val updateProjectAction = AtomicLazyProperty {
    UpdateProjectActionService.getInstance().getDefault()
  }
  private val absoluteDateTypeStyle = AtomicLazyProperty {
    AbsoluteDateTimeStyle.FROM_LOCALE
  }

  override fun getContent(): JComponent {
    return panel
  }

  override fun init() {
    val presentationStatusBarPreview = JBLabel()
    val presentationProjectViewPreview = JBLabel()
    val presentationBehindTrackerPreview = JBLabel()
    val updatePresentationPreviews: (StatusPresenter) -> Unit = {
      presentationStatusBarPreview.text = PresenterPreview.getStatusBarPreview(it)
      presentationProjectViewPreview.text = PresenterPreview.getProjectViewPreview(it)
      presentationBehindTrackerPreview.text = PresenterPreview.getBehindTrackerPreview(it)
    }

    val updateActionComment = MultiLineLabel(ResBundle.message("update.project.action.description"))
    panel = panel(LCFlags.fillX) {
      row(ResBundle.message("configurable.app.presentation.label")) {
        val renderer: ListCellRenderer<StatusPresenter?> = SimpleListCellRenderer.create("") { it?.label }
        val combo = comboBox(
          DefaultComboBoxModel(StatusPresenters.allPresenters()),
          presentationMode::get,
          { presentationMode.set(it!!) },
          renderer
        ).component
        combo.addActionListener {
          val presenter = combo.selectedItem as StatusPresenter
          appPages.statusPresenter = presenter
          updatePresentationPreviews(presenter)
        }
      }
      row(separated = true) {
        row {
          label(ResBundle.message("configurable.app.presentation.statusbar.preview"))
          presentationStatusBarPreview()
        }
        row {
          label(ResBundle.message("configurable.app.presentation.projectView.preview"))
          presentationProjectViewPreview()
        }
        row {
          label(ResBundle.message("configurable.app.presentation.behindTracker.preview"))
          presentationBehindTrackerPreview()
        }
      }
      row {
        checkBox(
          ResBundle.message("configurable.app.showStatusWidget.label"),
          showStatusWidget::get,
          showStatusWidget::set
        )
      }
      row {
        checkBox(
          ResBundle.message("configurable.app.trackChanges.label"),
          showChangesInStatusBar::get,
          showChangesInStatusBar::set
        )
      }
      row {
        checkBox(
          ResBundle.message("configurable.app.showBlame.label"),
          showBlameWidget::get,
          showBlameWidget::set
        )
      }
      row {
        checkBox(
          ResBundle.message("configurable.app.showEditorInlineBlame.label"),
          showEditorInlineBlame::get,
          showEditorInlineBlame::set
        )
      }
      row {
        checkBox(
          ResBundle.message("configurable.app.showProjectViewDecoration.label"),
          showProjectViewDecoration::get,
          showProjectViewDecoration::set
        )
      }
      row {
        checkBox(
          ResBundle.message("configurable.app.behindTrackerEnabled.label"),
          behindTracker::get,
          behindTracker::set
        )
      }
      row(ResBundle.message("update.project.action.label")) {
        val values = UpdateProjectActionService.getInstance().getAll()
        val renderer: ListCellRenderer<UpdateProjectAction?> = SimpleListCellRenderer.create("") { it?.getName() }
        comboBox(
          DefaultComboBoxModel(Vector(values)),
          updateProjectAction::get,
          { updateProjectAction.set(it!!) },
          renderer
        )
        row {
          updateActionComment()
        }
      }
      row(ResBundle.message("configurable.app.absoluteDateTimeStyle.label")) {
        comboBox(
          DefaultComboBoxModel(AbsoluteDateTimeStyle.values()),
          absoluteDateTypeStyle::get,
          { absoluteDateTypeStyle.set(it!!) },
          AbsoluteDateTimeStyleRenderer()
        )
      }
    }
  }

  override fun fillFromState(state: MutableConfig) {
    presentationMode.set(state.app.getPresenter())
    showStatusWidget.set(state.app.showStatusWidget)
    showChangesInStatusBar.set(state.app.showChangesInStatusBar)
    showBlameWidget.set(state.app.showBlameWidget)
    showEditorInlineBlame.set(state.app.showEditorInlineBlame)
    showProjectViewDecoration.set(state.app.showProjectViewStatus)
    behindTracker.set(state.app.behindTracker)
    updateProjectAction.set(state.app.getUpdateProjectAction())
    absoluteDateTypeStyle.set(state.app.absoluteDateTimeStyle)
  }

  override fun afterStateSet() {
    panel.reset()
  }

  override fun isModified(): Boolean {
    return panel.isModified()
  }

  override fun applyToState(state: MutableConfig) {
    panel.apply()
    // TODO: copy to state
  }
}

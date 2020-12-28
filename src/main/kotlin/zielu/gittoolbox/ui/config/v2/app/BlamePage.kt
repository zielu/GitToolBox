package zielu.gittoolbox.ui.config.v2.app

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.layout.panel
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.AuthorNameType
import zielu.gittoolbox.config.DateType
import zielu.gittoolbox.config.MutableConfig
import zielu.intellij.ui.GtFormUiEx
import javax.swing.JComponent
import javax.swing.ListCellRenderer

internal class BlamePage : GtFormUiEx<MutableConfig> {
  private val inlineAuthorNameType = AtomicLazyProperty {
    AuthorNameType.LASTNAME
  }
  private val inlineDateType = AtomicLazyProperty {
    DateType.AUTO
  }
  private val inlineShowSubject = AtomicBooleanProperty(true)
  private val inlineAlwaysShowBlameWhileDebugging = AtomicBooleanProperty(false)
  private val statusAuthorNameType = AtomicLazyProperty {
    AuthorNameType.LASTNAME
  }
  private lateinit var panel: DialogPanel

  override fun init() {
    val authorNameRenderer: ListCellRenderer<AuthorNameType?> = SimpleListCellRenderer.create("") {
      it?.getDisplayLabel()
    }
    panel = panel {
      titledRow(ResBundle.message("configurable.app.editorInlineBlame.label")) {
        row(ResBundle.message("configurable.app.blameAuthorName")) {
          comboBox(
            CollectionComboBoxModel(AuthorNameType.allValues),
            inlineAuthorNameType::get,
            { inlineAuthorNameType.set(it!!) },
            authorNameRenderer
          )
        }
        row(ResBundle.message("configurable.app.blameDateType")) {
          val renderer: ListCellRenderer<DateType?> = SimpleListCellRenderer.create("") {
            it?.getDisplayLabel()
          }
          comboBox(
            CollectionComboBoxModel(DateType.allValues),
            inlineDateType::get,
            { inlineDateType.set(it!!) },
            renderer
          )
        }
        row {
          checkBox(
            ResBundle.message("configurable.app.blameSubject"),
            inlineShowSubject::get,
            { inlineShowSubject.set(it) }
          )
        }
        row {
          checkBox(
            ResBundle.message("configurable.app.alwaysShowInlineBlameWhileDebugging.label"),
            inlineAlwaysShowBlameWhileDebugging::get,
            { inlineAlwaysShowBlameWhileDebugging.set(it) }
          )
        }
      }
      titledRow(ResBundle.message("configurable.app.statusBlame.label")) {
        row(ResBundle.message("configurable.app.blameAuthorName")) {
          comboBox(
            CollectionComboBoxModel(AuthorNameType.allValues),
            statusAuthorNameType::get,
            { statusAuthorNameType.set(it!!) },
            authorNameRenderer
          )
        }
      }
    }
  }

  override fun fillFromState(state: MutableConfig) {
    inlineAuthorNameType.set(state.app.blameInlineAuthorNameType)
    inlineDateType.set(state.app.blameInlineDateType)
    inlineShowSubject.set(state.app.blameInlineShowSubject)
    inlineAlwaysShowBlameWhileDebugging.set(state.app.alwaysShowInlineBlameWhileDebugging)
    statusAuthorNameType.set(state.app.blameStatusAuthorNameType)
    // TODO: fill from state
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

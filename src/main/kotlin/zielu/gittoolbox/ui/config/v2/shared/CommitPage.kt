package zielu.gittoolbox.ui.config.v2.shared

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.openapi.util.Disposer
import com.intellij.ui.CollectionListModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.panel
import org.jdesktop.swingx.action.AbstractActionExt
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.ResBundle.message
import zielu.gittoolbox.completion.FormatterIcons
import zielu.gittoolbox.config.CommitCompletionConfig
import zielu.gittoolbox.config.CommitCompletionMode
import zielu.gittoolbox.config.CommitCompletionType
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.ui.config.CommitCompletionConfigForm
import zielu.gittoolbox.ui.config.GtPatternFormatterForm
import zielu.gittoolbox.ui.config.v2.props.BoolProp
import zielu.gittoolbox.ui.config.v2.props.BoolPropWithOverride
import zielu.gittoolbox.ui.config.v2.props.UiItems
import zielu.gittoolbox.ui.config.v2.props.ValueProp
import zielu.gittoolbox.ui.config.v2.props.ValuePropWithOverride
import zielu.intellij.ui.GtFormUiEx
import java.awt.GridLayout
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListCellRenderer

internal class CommitPage : GtFormUiEx<MutableConfig> {
  private val commitDialogCompletionMode = AtomicLazyProperty {
    CommitCompletionMode.AUTOMATIC
  }
  private val commitDialogBranchCompletion = AtomicBooleanProperty(true)
  private val commitDialogGitmojiCompletion = AtomicBooleanProperty(false)
  private val commitMessageValidation = AtomicBooleanProperty(false)
  private val commitMessageValidationRegex = AtomicLazyProperty {
    ""
  }
  private val commitDialogBranchCompletionOverride = AtomicBooleanProperty(false)
  private val commitDialogGitmojiCompletionOverride = AtomicBooleanProperty(false)
  private val commitMessageValidationOverride = AtomicBooleanProperty(false)
  private val commitCompletionFormattersOverride = AtomicBooleanProperty(false)

  private val commitCompletionFormatters = CollectionListModel<CommitCompletionConfig>()
  private var completionFormattersContent = listOf<CommitCompletionConfig>()
  private lateinit var panel: DialogPanel
  private lateinit var addSimpleFormatterAction: Action
  private lateinit var completionFormattersPatternForm: GtPatternFormatterForm

  private lateinit var commitCompletionModeRow: Row
  private lateinit var commitDialogBranchCompletionOverrideCheckBox: JBCheckBox
  private lateinit var commitDialogGitmojiCompletionOverrideCheckBox: JBCheckBox
  private lateinit var commitMessageValidationOverrideCheckBox: JBCheckBox
  private lateinit var commitCompletionFormattersOverrideCheckBox: JBCheckBox

  private var hasProject: Boolean = false
  private val overrideCheckBoxes = OverrideCheckBoxes()
  private val uiItems = UiItems()
  init {
    Disposer.register(this, uiItems)
  }

  override fun init() {
    val completionFormattersList = JBList(commitCompletionFormatters)
    completionFormattersList.cellRenderer = SimpleListCellRenderer.create { label, value, _ ->
      label.text = value.presentableText
      if (value.type == CommitCompletionType.SIMPLE) {
        label.icon = FormatterIcons.Simple
      } else if (value.type == CommitCompletionType.PATTERN) {
        label.icon = FormatterIcons.RegExp
      }
    }
    val completionDecorator = ToolbarDecorator.createDecorator(completionFormattersList)
    val completionAddPopup = JBPopupMenu()
    addSimpleFormatterAction = createAddSimpleCompletionAction()
    completionAddPopup.add(addSimpleFormatterAction)
    completionAddPopup.add(createAddIssueCompletionAction())
    completionAddPopup.add(createAddPatternCompletionAction())
    completionDecorator.setAddAction {
      it.preferredPopupPoint?.let { relativePoint: RelativePoint ->
        completionAddPopup.show(relativePoint.component, relativePoint.point.x, relativePoint.point.y)
      }
    }
    completionDecorator.setAddActionName(ResBundle.message("commit.dialog.completion.formatters.add.tooltip"))
    completionDecorator.setRemoveAction {
      onRemoveCompletionFormatter(completionFormattersList)
      updateCompletionFormatterAddActions()
    }
    completionDecorator.setRemoveActionName(ResBundle.message("commit.dialog.completion.formatters.remove.tooltip"))

    val completionDecoratorPanel = completionDecorator.createPanel()
    completionFormattersPatternForm = GtPatternFormatterForm()
    Disposer.register(this, completionFormattersPatternForm)
    completionFormattersPatternForm.init()
    completionFormattersList.selectionModel.addListSelectionListener {
      if (!it.valueIsAdjusting) {
        onCompletionFormatterSelected(completionFormattersList.selectedValue)
      }
    }
    val completionFormattersPanel = JPanel(GridLayout(1, 2, 5, 0))
    completionFormattersPanel.add(completionDecoratorPanel)
    completionFormattersPanel.add(completionFormattersPatternForm.content)

    panel = panel {
      commitCompletionModeRow = row(message("commit.dialog.completion.mode.label")) {
        val renderer: ListCellRenderer<CommitCompletionMode?> = SimpleListCellRenderer.create("") { it?.displayLabel }
        comboBox(
          DefaultComboBoxModel(CommitCompletionMode.values()),
          commitDialogCompletionMode::get,
          { commitDialogCompletionMode.set(it!!) },
          renderer
        )
      }
      row {
        checkBox(
          message("commit.dialog.completion.branch.enabled.label"),
          commitDialogBranchCompletion::get,
          { commitDialogBranchCompletion.set(it) }
        )
        right {
          commitDialogBranchCompletionOverrideCheckBox = checkBox(
            ResBundle.message("common.override"),
            commitDialogBranchCompletionOverride::get,
            commitDialogBranchCompletionOverride::set
          ).component
          overrideCheckBoxes.register(commitDialogBranchCompletionOverrideCheckBox)
        }
      }
      row {
        checkBox(
          message("commit.dialog.completion.gitmoji.enabled.label"),
          commitDialogGitmojiCompletion::get,
          { commitDialogGitmojiCompletion.set(it) }
        )
        right {
          commitDialogGitmojiCompletionOverrideCheckBox = checkBox(
            ResBundle.message("common.override"),
            commitDialogGitmojiCompletionOverride::get,
            commitDialogGitmojiCompletionOverride::set
          ).component
          overrideCheckBoxes.register(commitDialogGitmojiCompletionOverrideCheckBox)
        }
      }
      row {
        cell {
          checkBox(
            message("commit.message.validation.enabled.label"),
            commitMessageValidation::get,
            { commitMessageValidation.set(it) }
          )
          expandableTextField(
            commitMessageValidationRegex::get,
            { commitMessageValidationRegex.set(it) }
          )
        }
        right {
          commitMessageValidationOverrideCheckBox = checkBox(
            ResBundle.message("common.override"),
            commitMessageValidationOverride::get,
            commitMessageValidationOverride::set
          ).component
          overrideCheckBoxes.register(commitMessageValidationOverrideCheckBox)
        }
      }
      titledRow(message("commit.dialog.completion.formatters.label")) {
        row {
          commitCompletionFormattersOverrideCheckBox = checkBox(
            ResBundle.message("common.override"),
            commitCompletionFormattersOverride::get,
            commitCompletionFormattersOverride::set
          ).component
          overrideCheckBoxes.register(commitCompletionFormattersOverrideCheckBox)
        }
        row {
          completionFormattersPanel(CCFlags.growX)
        }
      }
    }

    overrideCheckBoxes.hide()
  }

  private fun createAddSimpleCompletionAction(): Action {
    return object : AbstractActionExt() {
      init {
        name = message("commit.dialog.completion.formatters.simple.add.label")
        smallIcon = FormatterIcons.Simple
      }

      override fun actionPerformed(e: ActionEvent) {
        commitCompletionFormatters.add(CommitCompletionConfig.create(CommitCompletionType.SIMPLE))
        updateCompletionFormatterAddActions()
      }
    }
  }

  private fun createAddIssueCompletionAction(): Action {
    return object : AbstractActionExt() {
      init {
        name = message("commit.dialog.completion.formatters.pattern.issue.add.label")
        smallIcon = FormatterIcons.RegExp
      }

      override fun actionPerformed(e: ActionEvent) {
        commitCompletionFormatters.add(CommitCompletionConfig.createIssue())
        updateCompletionFormatterAddActions()
      }
    }
  }

  private fun createAddPatternCompletionAction(): Action {
    return object : AbstractActionExt() {
      init {
        name = message("commit.dialog.completion.formatters.pattern.add.label")
        smallIcon = FormatterIcons.RegExp
      }

      override fun actionPerformed(e: ActionEvent) {
        commitCompletionFormatters.add(CommitCompletionConfig.create(CommitCompletionType.PATTERN))
        updateCompletionFormatterAddActions()
      }
    }
  }

  private fun updateCompletionFormatterAddActions() {
    addSimpleFormatterAction.isEnabled = commitCompletionFormatters.items.any {
      it.type == CommitCompletionType.SIMPLE
    }
  }

  private fun onRemoveCompletionFormatter(list: JBList<CommitCompletionConfig>) {
    val index = list.selectedIndex
    if (index >= 0) {
      commitCompletionFormatters.removeRow(index)
    }
  }

  private fun onCompletionFormatterSelected(config: CommitCompletionConfig?) {
    if (config == null || config.type == CommitCompletionType.SIMPLE) {
      completionFormattersPatternForm.content.isVisible = false
    } else {
      completionFormattersPatternForm.content.isVisible = true
      completionFormattersPatternForm.setData(CommitCompletionConfigForm(config))
      completionFormattersPatternForm.afterStateSet()
    }
  }

  override fun fillFromState(state: MutableConfig) {
    hasProject = state.hasProject()
    uiItems.clear()

    if (hasProject) {
      commitCompletionModeRow.visible = false
      uiItems.register(
        BoolPropWithOverride(
          commitDialogBranchCompletion,
          commitDialogBranchCompletionOverride,
          state.app::commitDialogCompletion,
          state.prj().commitDialogBranchCompletionOverride
        ),
        BoolPropWithOverride(
          commitDialogGitmojiCompletion,
          commitDialogGitmojiCompletionOverride,
          state.app::commitDialogGitmojiCompletion,
          state.prj().commitDialogGitmojiCompletionOverride
        ),
        BoolPropWithOverride(
          commitMessageValidation,
          commitMessageValidationOverride,
          state.app::commitMessageValidationEnabled,
          state.prj().commitMessageValidationOverride
        ),
        ValuePropWithOverride(
          commitMessageValidationRegex,
          commitMessageValidationOverride,
          state.app::commitMessageValidationRegex,
          state.prj().commitMessageValidationRegexOverride::enabled,
          state.prj().commitMessageValidationRegexOverride::value
        )
      )
    } else {
      uiItems.register(
        ValueProp(
          commitDialogCompletionMode,
          state.app::commitDialogCompletionMode
        ),
        BoolProp(
          commitDialogBranchCompletion,
          state.app::commitDialogCompletion
        ),
        BoolProp(
          commitDialogGitmojiCompletion,
          state.app::commitDialogGitmojiCompletion
        ),
        BoolProp(
          commitMessageValidation,
          state.app::commitMessageValidationEnabled
        ),
        ValueProp(
          commitMessageValidationRegex,
          state.app::commitMessageValidationRegex
        )
      )
      completionFormattersContent = state.app.completionConfigs.map { it.copy() }
    }

    commitCompletionFormatters.replaceAll(completionFormattersContent)
    onCompletionFormatterSelected(null)
  }

  override fun isModified(): Boolean {
    return panel.isModified() || commitCompletionFormatters.items != completionFormattersContent
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

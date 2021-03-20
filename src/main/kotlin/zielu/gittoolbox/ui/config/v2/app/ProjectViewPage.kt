package zielu.gittoolbox.ui.config.v2.app

import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.ui.CollectionListModel
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import jodd.util.StringBand
import org.jdesktop.swingx.action.AbstractActionExt
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.DecorationPartConfig
import zielu.gittoolbox.config.DecorationPartType
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.ui.config.v2.DecorationPartPreview
import zielu.gittoolbox.ui.config.v2.props.ListValueProp
import zielu.gittoolbox.ui.config.v2.props.UiItems
import zielu.gittoolbox.ui.util.ListDataAnyChangeAdapter
import zielu.intellij.ui.GtFormUiEx
import java.awt.Component
import java.awt.GridLayout
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.ListDataEvent
import javax.swing.event.ListSelectionEvent

internal class ProjectViewPage(
  private val appPages: AppPages
) : GtFormUiEx<MutableConfig> {
  private val decorationPartActions: MutableMap<DecorationPartType, Component> = mutableMapOf()
  private val decorationLayoutModel = CollectionListModel<DecorationPartConfig>()
  private val layoutPreviewTextField = JBTextField()
  private lateinit var panel: DialogPanel
  private val uiItems = UiItems()

  init {
    layoutPreviewTextField.isEditable = false
  }

  override fun init() {
    val decorationPartsPreview: () -> String = {
      decorationLayoutModel.items.joinToString(" ") { getDecorationPartPreview(it) }
    }

    val updateDecorationPreview: () -> Unit = {
      layoutPreviewTextField.text = decorationPartsPreview()
    }

    val addDecorationLayoutPartPopup = JBPopupMenu()
    val decorationLayoutList = JBList(decorationLayoutModel)
    decorationLayoutList.cellRenderer = SimpleListCellRenderer.create("") {
      it.prefix + it.type.placeholder + it.postfix
    }
    val toolbarDecorator = ToolbarDecorator.createDecorator(decorationLayoutList)
    toolbarDecorator.setAddAction {
      it.preferredPopupPoint?.let { relativePoint: RelativePoint ->
        addDecorationLayoutPartPopup.show(relativePoint.component, relativePoint.point.x, relativePoint.point.y)
      }
    }
    toolbarDecorator.setRemoveAction {
      decorationLayoutList.selectedValuesList.forEach {
        addDecorationLayoutPartPopup.add(decorationPartActions[it.type])
        decorationLayoutModel.remove(it)
      }
      updateDecorationPreview()
    }

    val decoratorPanel = toolbarDecorator.createPanel()
    val prefixTextField = JBTextField()
    val postfixTextField = JBTextField()
    val decoratorDetailsPanel = panel {
      row(ResBundle.message("configurable.app.decorationPart.prefix.label")) {
        prefixTextField()
      }
      row(ResBundle.message("configurable.app.decorationPart.postfix.label")) {
        postfixTextField()
      }
    }
    decoratorDetailsPanel.border = BorderFactory.createEmptyBorder(3, 5, 5, 5)
    val configPanel = JPanel(GridLayout(1, 2))
    configPanel.add(decoratorPanel)
    configPanel.add(decoratorDetailsPanel)

    panel = panel(LCFlags.fillX) {
      row {
        label(ResBundle.message("configurable.app.decorationPart.layout.label"))
      }
      row {
        configPanel(CCFlags.growX)
      }
      row(ResBundle.message("configurable.app.decorationPart.layoutPreview.label")) {
        layoutPreviewTextField()
      }
    }

    DecorationPartType.getValues().forEach { type: DecorationPartType ->
      val action: Action = object : AbstractActionExt(type.label) {
        override fun actionPerformed(e: ActionEvent) {
          val config = DecorationPartConfig()
          config.type = type
          decorationLayoutModel.add(config)
          addDecorationLayoutPartPopup.remove(decorationPartActions[type])
          val lastAddedIndex: Int = decorationLayoutModel.size - 1
          decorationLayoutList.selectionModel.setSelectionInterval(lastAddedIndex, lastAddedIndex)
        }
      }
      decorationPartActions[type] = JMenuItem(action)
    }

    decorationLayoutModel.addListDataListener(object : ListDataAnyChangeAdapter() {
      override fun changed(e: ListDataEvent) {
        updateDecorationPreview()
      }
    })
    val currentDecorationPart: () -> DecorationPartConfig? = {
      val selectedIndices = decorationLayoutList.selectedIndices
      if (selectedIndices.isNotEmpty()) {
        val selected = selectedIndices[0]
        decorationLayoutModel.getElementAt(selected)
      } else {
        null
      }
    }
    decorationLayoutList.selectionModel.addListSelectionListener { event: ListSelectionEvent ->
      if (!event.valueIsAdjusting) {
        val selectedIndices = decorationLayoutList.selectedIndices
        val editorsEnabled = selectedIndices.size == 1
        prefixTextField.isEnabled = editorsEnabled
        postfixTextField.isEnabled = editorsEnabled
        currentDecorationPart()?.let {
          prefixTextField.text = it.prefix
          postfixTextField.text = it.postfix
        }
      }
    }
    prefixTextField.document.addDocumentListener(object : DocumentAdapter() {
      override fun textChanged(e: DocumentEvent) {
        currentDecorationPart()?.prefix = prefixTextField.text
        decorationLayoutList.repaint()
        updateDecorationPreview()
      }
    })
    postfixTextField.document.addDocumentListener(object : DocumentAdapter() {
      override fun textChanged(e: DocumentEvent) {
        currentDecorationPart()?.postfix = postfixTextField.text
        decorationLayoutList.repaint()
        updateDecorationPreview()
      }
    })

    appPages.addListener(object : AppPagesNotifier {
      override fun appPagesChanged(appPages: AppPages) {
        updateDecorationPreview()
      }
    })
  }

  private fun getDecorationPartPreview(config: DecorationPartConfig): String {
    return appPages.statusPresenter.let {
      val preview = StringBand(config.prefix)
      DecorationPartPreview
        .appendPreview(it, config.type, preview)
        .append(config.postfix)
      preview.toString()
    }
  }

  override fun afterStateSet() {
    panel.reset()
  }

  override fun fillFromState(state: MutableConfig) {
    uiItems.clear()

    uiItems.register(
      ListValueProp(
        decorationLayoutModel,
        state.app::decorationParts
      )
    )
  }

  override fun isModified(): Boolean {
    return panel.isModified() || uiItems.isModified()
  }

  override fun getContent(): JComponent {
    return panel
  }

  override fun applyToState(state: MutableConfig) {
    panel.apply()
    uiItems.apply()
  }
}

package zielu.gittoolbox.ui.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.util.AppUtil.updateSettingsAndSave

internal class InlineBlameToggleAction : ToggleAction() {
  override fun isSelected(e: AnActionEvent): Boolean = AppConfig.getConfig().showEditorInlineBlame

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    val current = AppConfig.getConfig()
    if (current.showEditorInlineBlame != state) {
      updateSettingsAndSave { it.showEditorInlineBlame = state }
    }
  }
}

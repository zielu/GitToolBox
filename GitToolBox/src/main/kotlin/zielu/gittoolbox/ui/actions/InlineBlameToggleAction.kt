package zielu.gittoolbox.ui.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.util.AppUtil.modifySettingsSaveAndNotify

internal class InlineBlameToggleAction : ToggleAction() {
  override fun isSelected(e: AnActionEvent): Boolean = AppConfig.get().showEditorInlineBlame

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    val current = AppConfig.get()
    if (current.showEditorInlineBlame != state) {
      modifySettingsSaveAndNotify { it.showEditorInlineBlame = state }
    }
  }
}

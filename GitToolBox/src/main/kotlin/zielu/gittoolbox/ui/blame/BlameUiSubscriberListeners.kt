package zielu.gittoolbox.ui.blame

import com.intellij.openapi.editor.colors.EditorColorsListener
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.blame.BlameListener
import zielu.gittoolbox.config.AppConfigNotifier
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.util.ProjectMessageBusListener

internal class BlameUiSubscriberBlameListener(
  project: Project
) : ProjectMessageBusListener(project), BlameListener {
  override fun blameUpdated(file: VirtualFile) {
    handleEvent { project ->
      BlameUiSubscriber.getInstance(project).onBlameUpdate(file)
    }
  }
}

internal class BlameUiSubscriberColorSchemeListener(
  project: Project
) : ProjectMessageBusListener(project), EditorColorsListener {
  override fun globalSchemeChange(scheme: EditorColorsScheme?) {
    scheme?.let { colorScheme ->
      handleEvent { project ->
        BlameUiSubscriber.getInstance(project).onColorSchemeChanged(colorScheme)
      }
    }
  }
}

internal class BlameUiSubscriberConfigListener(
  project: Project
) : ProjectMessageBusListener(project), AppConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfig2, current: GitToolBoxConfig2) {
    handleEvent { project ->
      BlameUiSubscriber.getInstance(project).onConfigChanged(previous, current)
    }
  }
}

internal class BlameUiSubscriberEditorListener(
  project: Project
) : ProjectMessageBusListener(project), FileEditorManagerListener {
  override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
    handleEvent { project ->
      BlameUiSubscriber.getInstance(project).onFileClosed(file)
    }
  }
}

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

internal class BlameUiSubscriberBlameListener(private val project: Project) : BlameListener {
  override fun blameUpdated(file: VirtualFile) {
    BlameUiSubscriber.getInstance(project).onBlameUpdate(file)
  }
}

internal class BlameUiSubscriberColorSchemeListener(private val project: Project) : EditorColorsListener {
  override fun globalSchemeChange(scheme: EditorColorsScheme?) {
    scheme?.let {
      BlameUiSubscriber.getInstance(project).onColorSchemeChanged(it)
    }
  }
}

internal class BlameUiSubscriberConfigListener(private val project: Project) : AppConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfig2, current: GitToolBoxConfig2) {
    BlameUiSubscriber.getInstance(project).onConfigChanged(previous, current)
  }
}

internal class BlameUiSubscriberEditorListener(private val project: Project) : FileEditorManagerListener {
  override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
    BlameUiSubscriber.getInstance(project).onFileClosed(file)
  }
}

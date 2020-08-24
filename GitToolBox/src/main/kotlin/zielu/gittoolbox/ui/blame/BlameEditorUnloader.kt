package zielu.gittoolbox.ui.blame

import com.intellij.openapi.Disposable
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project

internal class BlameEditorUnloader(
  private val project: Project
) : Disposable {
    override fun dispose() {
        FileEditorManager.getInstance(project).allEditors
          .asSequence()
          .filterIsInstance<TextEditor>()
          .map { textEditor -> textEditor.editor }
          .forEach {
              BlameEditorData.clear(it)
              BlameEditorLineData.clear(it)
              BlameStatusLineData.clear(it)
          }
    }
}

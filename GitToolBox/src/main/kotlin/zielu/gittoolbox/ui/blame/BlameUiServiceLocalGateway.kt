package zielu.gittoolbox.ui.blame

import com.codahale.metrics.Timer
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepositoryManager
import zielu.gittoolbox.blame.BlameCache
import zielu.gittoolbox.blame.BlameService
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.config.DecorationColors
import zielu.gittoolbox.metrics.ProjectMetrics
import zielu.gittoolbox.revision.RevisionInfo

internal class BlameUiServiceLocalGateway(
  private val project: Project,
  textAttributesKey: TextAttributesKey
) {
  val editorTimer: Timer by lazy {
    ProjectMetrics.getInstance(project).timer("blame-editor-painter")
  }
  val statusBarTimer: Timer by lazy {
    ProjectMetrics.getInstance(project).timer("blame-status-bar")
  }
  val editorInfoTimer: Timer by lazy {
    ProjectMetrics.getInstance(project).timer("blame-editor-get-info")
  }
  val statusBarInfoTimer: Timer by lazy {
    ProjectMetrics.getInstance(project).timer("blame-status-bar-get-info")
  }
  val defaultBlameTextAttributes: TextAttributes by lazy {
    DecorationColors.textAttributes(textAttributesKey)
  }

  fun isUnderGit(vFile: VirtualFile): Boolean {
    return VirtualFileRepoCache.getInstance(project).isUnderGitRoot(vFile)
  }

  fun getDocument(vFile: VirtualFile): Document? {
    return FileDocumentManager.getInstance().getDocument(vFile)
  }

  fun getEditorIfDocumentSelected(document: Document): Editor? {
    val editor = FileEditorManager.getInstance(project).selectedTextEditor
    return if (editor != null && editor.document == document) {
      editor
    } else null
  }

  fun getAllEditors(vFile: VirtualFile): Collection<Editor> {
    val fileEditors = FileEditorManager.getInstance(project).getAllEditors(vFile).toList()
    return fileEditors.asSequence()
      .filterIsInstance<TextEditor>()
      .map { textEditor -> textEditor.editor }
      .toList()
  }

  fun getCurrentLineIndex(editor: Editor): Int {
    return BlameUi.getCurrentLineIndex(editor)
  }

  fun getStatusDecoration(revisionInfo: RevisionInfo): String {
    return BlamePresenter.getInstance().getStatusBar(revisionInfo)
  }

  fun getEditorInlineDecoration(revisionInfo: RevisionInfo): String {
    return BlamePresenter.getInstance().getEditorInline(revisionInfo)
  }

  fun getBlameStatusTooltip(revisionInfo: RevisionInfo): String? {
    return revisionInfo.getSubject()
  }

  fun getLineBlame(document: Document, file: VirtualFile, editorLineIndex: Int): RevisionInfo {
    return BlameService.getInstance(project).getDocumentLineIndexBlame(document, file, editorLineIndex)
  }

  fun getLineBlame(lineInfo: LineInfo): RevisionInfo {
    return getLineBlame(lineInfo.document, lineInfo.file, lineInfo.index)
  }

  fun invalidateAllBlames() {
    val manager = GitRepositoryManager.getInstance(project)
    val repositories = manager.repositories.toList()
    val roots = repositories.map { it.root }
    if (roots.isNotEmpty()) {
      BlameCache.getExistingInstance(project).ifPresent { cache -> invalidateBlameForRoots(cache, roots) }
    }
  }

  private fun invalidateBlameForRoots(cache: BlameCache, roots: Collection<VirtualFile>) {
    roots.forEach { cache.invalidateForRoot(it) }
  }
}

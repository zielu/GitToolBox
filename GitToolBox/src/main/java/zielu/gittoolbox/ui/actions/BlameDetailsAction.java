package zielu.gittoolbox.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.config.AppConfig;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.ui.blame.BlameUi;

public class BlameDetailsAction extends AnAction {
  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
    e.getPresentation().setEnabled(isEnabled(e));
  }

  private boolean isEnabled(@NotNull AnActionEvent e) {
    GitToolBoxConfig2 config = AppConfig.get();
    if (!config.getShowBlameWidget() && !config.getShowEditorInlineBlame()) {
      return false;
    }
    Project project = e.getData(CommonDataKeys.PROJECT);
    if (project == null) {
      return false;
    }
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    if (editor != null) {
      Document document = editor.getDocument();
      if (!BlameUi.isDocumentInBulkUpdate(document)) {
        VirtualFile editorFile = FileDocumentManager.getInstance().getFile(document);
        if (editorFile != null) {
          return VirtualFileRepoCache.getInstance(project).isUnderGitRoot(editorFile);
        }
      }
    }
    return false;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getData(CommonDataKeys.PROJECT);
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    if (project != null && editor != null) {
      VirtualFile editorFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
      if (editorFile != null) {
        int currentLineIndex = BlameUi.getCurrentLineIndex(editor);
        if (BlameUi.isValidLineIndex(currentLineIndex)) {
          BlameService blameService = BlameService.getInstance(project);
          RevisionInfo revisionInfo = blameService.getDocumentLineIndexBlame(editor.getDocument(), editorFile,
              currentLineIndex);
          if (revisionInfo.isNotEmpty()) {
            BlameUi.showBlamePopup(editor, editorFile, revisionInfo);
          }
        }
      }
    }
  }
}

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
    GitToolBoxConfig2 toolBoxConfig2 = GitToolBoxConfig2.getInstance();
    if (!toolBoxConfig2.showBlame && !toolBoxConfig2.showEditorInlineBlame) {
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
        int currentLine = BlameUi.getCurrentLineNumber(editor);
        if (BlameUi.isValidLineNumber(currentLine)) {
          BlameService blameService = BlameService.getInstance(project);
          RevisionInfo revisionInfo = blameService.getDocumentLineBlame(editor.getDocument(), editorFile, currentLine);
          if (revisionInfo.isNotEmpty()) {
            String detailsText = revisionInfo.getDetails();
            if (detailsText != null) {
              BlameUi.showBlamePopup(editor, editorFile, revisionInfo);
            }
          }
        }
      }
    }
  }
}

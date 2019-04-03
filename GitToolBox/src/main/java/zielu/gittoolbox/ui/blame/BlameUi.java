package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionInfo;

public final class BlameUi {
  private static final int NO_LINE = Integer.MIN_VALUE;

  private BlameUi() {
    //do nothing
  }

  public static void showBlamePopup(@NotNull Editor editor, @NotNull VirtualFile file,
                                    @NotNull RevisionInfo revisionInfo) {
    Project project = editor.getProject();
    if (project != null) {
      new BlamePopup(project, file, revisionInfo).showFor(editor.getComponent());
    }
  }

  public static boolean isDocumentInBulkUpdate(@Nullable Document document) {
    if (document instanceof DocumentEx) {
      DocumentEx docEx = (DocumentEx) document;
      return docEx.isInBulkUpdate();
    }
    return false;
  }

  public static int getCurrentLineIndex(@NotNull Editor editor) {
    CaretModel caretModel = editor.getCaretModel();
    if (!caretModel.isUpToDate()) {
      return NO_LINE;
    }
    LogicalPosition position = caretModel.getLogicalPosition();
    return position.line;
  }

  public static boolean isValidLineIndex(int lineNumber) {
    return lineNumber != NO_LINE;
  }
}

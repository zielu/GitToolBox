package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.blame.Blame;

public final class BlameUi {
  private BlameUi() {
    //do nothing
  }

  public static void showBlamePopup(@NotNull Editor editor, @NotNull VirtualFile file, @NotNull Blame blame) {
    new BlamePopup(editor.getProject(), file, blame).showFor(editor.getComponent());
  }

  public static boolean isDocumentInBulkUpdate(@Nullable Document document) {
    if (document instanceof DocumentEx) {
      DocumentEx docEx = (DocumentEx) document;
      return docEx.isInBulkUpdate();
    }
    return false;
  }

  public static int getCurrentLineNumber(@NotNull Editor editor) {
    CaretModel caretModel = editor.getCaretModel();
    if (!caretModel.isUpToDate()) {
      return Integer.MIN_VALUE;
    }
    LogicalPosition position = caretModel.getLogicalPosition();
    return position.line;
  }
}

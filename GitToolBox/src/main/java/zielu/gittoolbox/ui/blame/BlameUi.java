package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupAdapter;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import javax.swing.JTextArea;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;

public final class BlameUi {
  private BlameUi() {
    //do nothing
  }

  public static void showBlameDetails(@NotNull Editor editor, @NotNull String blameDetails) {
    JTextArea content = new JTextArea(blameDetails);
    content.setEditable(false);
    Balloon balloon = JBPopupFactory.getInstance()
        .createDialogBalloonBuilder(content, ResBundle.getString("statusBar.blame.popup.title"))
        .setDialogMode(true)
        .setCloseButtonEnabled(false)
        .setHideOnClickOutside(true)
        .setShowCallout(false)
        .createBalloon();
    balloon.addListener(new JBPopupAdapter() {
      @Override
      public void onClosed(@NotNull LightweightWindowEvent event) {
        if (!balloon.isDisposed()) {
          balloon.dispose();
        }
      }
    });
    balloon.showInCenterOf(editor.getComponent());
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

package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import javax.swing.JTextArea;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;

public final class BlameUi {
  private BlameUi() {
    //do nothing
  }

  public static void showBlameDetails(@NotNull Editor editor, @NotNull String blameDetails) {
    JTextArea content = new JTextArea(blameDetails);
    content.setEditable(false);
    JBPopupFactory.getInstance()
        .createDialogBalloonBuilder(content, ResBundle.getString("statusBar.blame.popup.title"))
        .setDialogMode(true)
        .setCloseButtonEnabled(false)
        .setHideOnClickOutside(true)
        .setShowCallout(false)
        .createBalloon().showInCenterOf(editor.getComponent());
  }
}

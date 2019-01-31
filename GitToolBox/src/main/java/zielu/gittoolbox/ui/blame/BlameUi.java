package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.ui.popup.ActiveIcon;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupAdapter;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import java.awt.BorderLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.ResIcons;
import zielu.gittoolbox.blame.Blame;

public final class BlameUi {
  private BlameUi() {
    //do nothing
  }

  public static void showBlamePopup(@NotNull Editor editor, @NotNull Blame blame) {
    showBlameBalloon(editor, blame);
  }

  private static void showBlameBalloon(@NotNull Editor editor, @NotNull Blame blame) {
    String text = "<pre>" + blame.getDetailedText() + "</pre><br/>"
        + "<a href='reveal-in-log'>Reveal in Log</a>&nbsp;&nbsp;&nbsp"
        + "<a href='affected-files'>Affected Files</a>";
    HyperlinkAdapter linkHandler = new HyperlinkAdapter() {
      @Override
      protected void hyperlinkActivated(HyperlinkEvent e) {
        String action = e.getDescription();
        if ("reveal-in-log".equalsIgnoreCase(action)) {

        } else if ("affected-files".equalsIgnoreCase(action)) {

        }
      }
    };
    Balloon balloon = JBPopupFactory.getInstance()
        .createHtmlTextBalloonBuilder(text, null, JBColor.LIGHT_GRAY, linkHandler)
        .setTitle(ResBundle.getString("statusBar.blame.popup.title"))
        .setShowCallout(false)
        .createBalloon();
    balloon.addListener(new JBPopupAdapter() {
      @Override
      public void onClosed(@NotNull LightweightWindowEvent event) {
        if (!balloon.isDisposed()) {
          Disposer.dispose(balloon);
        }
      }
    });
    balloon.showInCenterOf(editor.getComponent());
  }

  private static void showBlameComponentPopup(@NotNull Editor editor, @NotNull Blame blame) {
    JEditorPane content = new JTextPane();
    content.setText(blame.getDetailedText());
    content.setEditable(false);
    JPanel panel = new JPanel(new BorderLayout());
    JBScrollPane scroll = new JBScrollPane(content);
    panel.add(scroll, BorderLayout.CENTER);
    JBPopup popup = JBPopupFactory.getInstance()
        .createComponentPopupBuilder(panel, content)
        .setProject(editor.getProject())
        .setTitle(ResBundle.getString("statusBar.blame.popup.title"))
        .setTitleIcon(new ActiveIcon(ResIcons.Blame))
        .setShowBorder(true)
        .setShowShadow(true)
        .setResizable(true)
        .createPopup();
    popup.showInCenterOf(editor.getComponent());
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

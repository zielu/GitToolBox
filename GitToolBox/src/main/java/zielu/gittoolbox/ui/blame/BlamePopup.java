package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupAdapter;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.JBColor;
import com.intellij.vcs.log.impl.VcsLogContentUtil;
import java.awt.datatransfer.StringSelection;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.blame.Blame;

class BlamePopup {
  private static final String REVEAL_IN_LOG = "reveal-in-log";
  private static final String AFFECTED_FILES = "affected-files";
  private static final String COPY_REVISION = "copy-revision";

  private final Editor editor;
  private final Blame blame;

  private Balloon balloon;

  BlamePopup(@NotNull Editor editor, @NotNull Blame blame) {
    this.editor = editor;
    this.blame = blame;
  }

  void show() {
    balloon = JBPopupFactory.getInstance()
        .createHtmlTextBalloonBuilder(prepareText(), null, JBColor.LIGHT_GRAY, createLinkListener())
        .setTitle(ResBundle.getString("statusBar.blame.popup.title"))
        .setAnimationCycle(200)
        .setShowCallout(false)
        .setCloseButtonEnabled(true)
        .setHideOnCloseClick(true)
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

  private String prepareText() {
    return  "<pre>" + blame.getDetailedText() + "</pre><br/>"
        + "<a href='" + REVEAL_IN_LOG + "'>Git Log</a>&nbsp;&nbsp;&nbsp"
        + "<a href='" + AFFECTED_FILES + "'>Affected Files</a>&nbsp;&nbsp;&nbsp"
        + "<a href='" + COPY_REVISION + "'>Copy Revision</a>";
  }

  private HyperlinkListener createLinkListener() {
    return new HyperlinkAdapter() {
      @Override
      protected void hyperlinkActivated(HyperlinkEvent e) {
        handleLinkClick(e.getDescription());
      }
    };
  }

  private void handleLinkClick(String action) {
    if (REVEAL_IN_LOG.equalsIgnoreCase(action)) {
      VcsLogContentUtil.openMainLogAndExecute(editor.getProject(),
          logUi -> logUi.getVcsLog().jumpToReference(blame.getRevisionNumber().asString()));
      //do nothing
    } else if (AFFECTED_FILES.equalsIgnoreCase(action)) {
      //com.intellij.openapi.vcs.actions.ShowAnnotateOperationsPopup.ShowAffectedFilesAction.actionPerformed
    } else if (COPY_REVISION.equalsIgnoreCase(action)) {
      CopyPasteManager.getInstance().setContents(new StringSelection(blame.getRevisionNumber().asString()));
    }
    close();
  }

  private void close() {
    if (balloon != null) {
      balloon.hide(true);
    }
  }
}

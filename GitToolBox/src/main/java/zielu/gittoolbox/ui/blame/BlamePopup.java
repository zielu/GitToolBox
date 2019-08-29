package zielu.gittoolbox.ui.blame;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupAdapter;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.CommittedChangesProvider;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.issueLinks.IssueLinkHtmlRenderer;
import com.intellij.openapi.vcs.impl.AbstractVcsHelperImpl;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.Gray;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.JBColor;
import com.intellij.vcs.log.impl.VcsLogContentUtil;
import com.intellij.vcs.log.ui.AbstractVcsLogUi;
import com.intellij.vcsUtil.VcsUtil;
import java.awt.datatransfer.StringSelection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.JComponent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.revision.RevisionService;
import zielu.intellij.ui.ZUiUtil;

class BlamePopup {
  private static final Logger LOG = Logger.getInstance(BlamePopup.class);

  private static final JBColor BACKGROUND = new JBColor(Gray._224, Gray._92);

  private final Project project;
  private final VirtualFile file;
  private final RevisionInfo revisionInfo;

  private Balloon balloon;

  BlamePopup(@NotNull Project project, @NotNull VirtualFile file, @NotNull RevisionInfo revisionInfo) {
    this.project = project;
    this.file = file;
    this.revisionInfo = revisionInfo;
  }

  void showFor(@NotNull JComponent component) {
    balloon = JBPopupFactory.getInstance()
        .createHtmlTextBalloonBuilder(prepareText(), null, BACKGROUND, createLinkListener())
        .setTitle(ResBundle.message("statusBar.blame.popup.title"))
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
    balloon.showInCenterOf(component);
  }

  private String prepareText() {
    String message = RevisionService.getInstance(project).getCommitMessage(file, revisionInfo);
    message = IssueLinkHtmlRenderer.formatTextWithLinks(project, message != null ? message : "");
    String commitInformation = BlamePresenter.getInstance().getPopup(revisionInfo, message);
    return ZUiUtil.asHtml(commitInformation
        + "<br><br>"
        + PopupAction.REVEAL_IN_LOG.createHtmlLink() + "&nbsp;&nbsp;&nbsp"
        + PopupAction.AFFECTED_FILES.createHtmlLink() + "&nbsp;&nbsp;&nbsp"
        + PopupAction.COPY_REVISION.createHtmlLink()
    );
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
    if (PopupAction.REVEAL_IN_LOG.isAction(action)) {
      VcsLogContentUtil.openMainLogAndExecute(project, this::revealRevisionInLog);
    } else if (PopupAction.AFFECTED_FILES.isAction(action)) {
      showAffectedFiles();
    } else if (PopupAction.COPY_REVISION.isAction(action)) {
      CopyPasteManager.getInstance().setContents(new StringSelection(revisionInfo.getRevisionNumber().asString()));
    } else {
      BrowserUtil.open(action);
    }
    close();
  }

  private void revealRevisionInLog(@NotNull AbstractVcsLogUi logUi) {
    String revisionNumber = revisionInfo.getRevisionNumber().asString();
    Future<Boolean> future = logUi.getVcsLog().jumpToReference(revisionNumber);
    if (!future.isDone()) {
      ProgressManager.getInstance().run(new Task.Backgroundable(project,
          "Searching for revision " + revisionNumber, false,
          PerformInBackgroundOption.ALWAYS_BACKGROUND) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          try {
            future.get();
          } catch (CancellationException | InterruptedException ignored) {
            Thread.currentThread().interrupt();
          } catch (ExecutionException e) {
            LOG.error(e);
          }
        }
      });
    }
  }

  private void showAffectedFiles() {
    AbstractVcs vcs = ProjectLevelVcsManager.getInstance(project).getVcsFor(file);
    if (vcs != null) {
      CommittedChangesProvider<?, ?> changesProvider = vcs.getCommittedChangesProvider();
      if (changesProvider != null) {
        Pair<? extends CommittedChangeList, FilePath> affectedFiles = findAffectedFiles(changesProvider);
        if (affectedFiles != null) {
          AbstractVcsHelperImpl.loadAndShowCommittedChangesDetails(project, revisionInfo.getRevisionNumber(),
              affectedFiles.getSecond(), () -> affectedFiles);
        }
      }
    }
  }

  @Nullable
  private Pair<? extends CommittedChangeList, FilePath> findAffectedFiles(
      @NotNull CommittedChangesProvider<?, ?> changesProvider) {
    try {
      Pair<? extends CommittedChangeList, FilePath> pair = changesProvider.getOneList(file,
          revisionInfo.getRevisionNumber());
      if (pair != null && pair.getFirst() != null) {
        if (pair.getSecond() != null) {
          pair = Pair.create(pair.getFirst(), VcsUtil.getFilePath(file));
        }
        return pair;
      }
      return null;
    } catch (VcsException e) {
      LOG.warn("Failed to find affected files for path " + file, e);
      return null;
    }
  }

  private void close() {
    if (balloon != null) {
      balloon.hide(true);
    }
  }

  private enum PopupAction {
    REVEAL_IN_LOG("reveal-in-log", "blame.popup.action.reveal.in.log.label"),
    AFFECTED_FILES("affected-files", "blame.popup.action.affected.files.label"),
    COPY_REVISION("copy-revision", "blame.popup.action.copy.revision.label")
    ;

    private final String key;
    private final String labelKey;

    PopupAction(String key, String labelKey) {
      this.key = key;
      this.labelKey = labelKey;
    }

    boolean isAction(String key) {
      return this.key.equalsIgnoreCase(key);
    }

    String createHtmlLink() {
      return "<a href='" + key + "'>" + ResBundle.message(labelKey) + "</a>";
    }
  }
}

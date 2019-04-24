package zielu.gittoolbox.ui.blame;

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
import com.intellij.openapi.vcs.impl.AbstractVcsHelperImpl;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
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

class BlamePopup {
  private static final Logger LOG = Logger.getInstance(BlamePopup.class);

  private static final String REVEAL_IN_LOG = "reveal-in-log";
  private static final String AFFECTED_FILES = "affected-files";
  private static final String COPY_REVISION = "copy-revision";

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
        .createHtmlTextBalloonBuilder(prepareText(), null, JBColor.LIGHT_GRAY, createLinkListener())
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
    String details = BlamePresenter.getInstance().getPopup(revisionInfo, message);
    return  "<pre>" + details + "</pre><br/>"
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
      VcsLogContentUtil.openMainLogAndExecute(project, this::revealRevisionInLog);
    } else if (AFFECTED_FILES.equalsIgnoreCase(action)) {
      showAffectedFiles();
    } else if (COPY_REVISION.equalsIgnoreCase(action)) {
      CopyPasteManager.getInstance().setContents(new StringSelection(revisionInfo.getRevisionNumber().asString()));
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
            //ignored
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
}

package zielu.gittoolbox.ui.statusbar;

import com.codahale.metrics.Timer;
import com.google.common.base.Ascii;
import com.google.common.base.Strings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import java.awt.Component;
import java.awt.event.MouseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.ProjectMetrics;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.ui.blame.BlamePresenter;
import zielu.gittoolbox.ui.blame.BlameUi;
import zielu.gittoolbox.ui.util.AppUiUtil;

public class BlameStatusWidget extends EditorBasedWidget implements StatusBarUi,
    StatusBarWidget.Multiframe, StatusBarWidget.TextPresentation {

  private static final String ID = BlameStatusWidget.class.getName();
  private static final int MAX_LENGTH = 27;
  private static final String MAX_POSSIBLE_TEXT = Strings.repeat("0", MAX_LENGTH);
  private final Timer updateForDocumentTimer;
  private final Timer updateForCaretTimer;
  private final Timer updateForSelectionTimer;
  private final BlameStatusGateway blameStatusGateway;
  private final BlameStateHolder stateHolder;
  private final Runnable blameDumbModeExitAction;
  private final Consumer<Document> bulkUpdateFinishedAction;
  private final Consumer<VirtualFile> blameUpdatedAction;
  private String blameText = ResBundle.na();
  private boolean visible;

  public BlameStatusWidget(@NotNull Project project) {
    super(project);
    stateHolder = new BlameStateHolder();
    Metrics metrics = ProjectMetrics.getInstance(project);
    updateForDocumentTimer = metrics.timer("blame-statusbar-update-for-document");
    updateForCaretTimer = metrics.timer("blame-statusbar-update-for-caret");
    updateForSelectionTimer = metrics.timer("blame-statusbar-update-for-selection");
    clearBlame();
    blameDumbModeExitAction = () -> {
      Editor editor = getEditor();
      if (editor != null) {
        updateForEditor(editor);
      }
    };
    bulkUpdateFinishedAction = this::updateForDocument;
    blameUpdatedAction = this::blameUpdate;
    blameStatusGateway = BlameStatusGateway.getInstance(project);
    blameStatusGateway.addDumbModeExitAction(blameDumbModeExitAction);
    blameStatusGateway.addBulkUpdateFinishedAction(bulkUpdateFinishedAction);
    blameStatusGateway.addBlameUpdatedAction(blameUpdatedAction);
  }

  @Override
  public void install(@NotNull StatusBar statusBar) {
    super.install(statusBar);
    EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();
    eventMulticaster.addDocumentListener(new DocumentListener() {
      @Override
      public void documentChanged(@NotNull DocumentEvent e) {
        Document document = e.getDocument();
        updateForDocument(document);
      }
    }, this);
    eventMulticaster.addCaretListener(new CaretListener() {
      @Override
      public void caretPositionChanged(@NotNull CaretEvent e) {
        Editor editor = e.getEditor();
        updateForCaretTimer.time(() -> updateForEditor(editor));
      }
    }, this);
  }

  private void blameUpdate(@NotNull VirtualFile file) {
    AppUiUtil.invokeLaterIfNeeded(myProject, () -> {
      if (file.equals(stateHolder.getCurrentFile())) {
        fileChanged(stateHolder.getCurrentEditor(), file);
      }
    });
  }

  private void updateForDocument(@Nullable Document document) {
    updateForDocumentTimer.time(() -> runUpdateForDocument(document));
  }

  private void runUpdateForDocument(@Nullable Document document) {
    if (isDocumentInvalid(document)) {
      return;
    }
    stateHolder.updateCurrentFile(document);
    if (shouldShow()) {
      VirtualFile currentFile = getCurrentFileUnderVcs();
      if (currentFile != null) {
        Editor selectedEditor = stateHolder.getCurrentEditor();
        fileChanged(selectedEditor, currentFile);
      } else {
        if (clearBlame()) {
          updateWidget();
        }
      }
    }
  }

  private boolean isDocumentInvalid(@Nullable Document document) {
    if (blameStatusGateway.isInBulkUpdate(document)) {
      return true;
    }
    return stateHolder.isCurrentEditorDocument(document);
  }

  @Nullable
  private VirtualFile getCurrentFileUnderVcs() {
    VirtualFile currentFile = stateHolder.getCurrentFile();
    if (currentFile != null && blameStatusGateway.isUnderVcs(currentFile)) {
      return currentFile;
    }
    return null;
  }

  private void updateForEditor(@NotNull Editor updatedEditor) {
    Editor selectedEditor = stateHolder.getCurrentEditor();
    if (selectedEditor == null || selectedEditor.getDocument() != updatedEditor.getDocument()) {
      return;
    }
    stateHolder.updateCurrentFile(updatedEditor.getDocument());
    updateForEditorWithCurrentFile(updatedEditor);
  }

  private void updateForEditorWithCurrentFile(@NotNull Editor updatedEditor) {
    if (shouldShow()) {
      VirtualFile currentFile = getCurrentFileUnderVcs();
      if (currentFile != null) {
        fileChanged(updatedEditor, currentFile);
      } else {
        if (clearBlame()) {
          updateWidget();
        }
      }
    }
  }

  @Override
  public StatusBarWidget copy() {
    return new BlameStatusWidget(myProject);
  }

  @NotNull
  @Override
  public String ID() {
    return ID;
  }

  @Nullable
  @Override
  public WidgetPresentation getPresentation(@NotNull PlatformType type) {
    return this;
  }

  @NotNull
  @Override
  public String getText() {
    String blamePart = Ascii.truncate(blameText, MAX_LENGTH, "...");
    blamePart = Strings.padEnd(blamePart, MAX_LENGTH, ' ');
    return blamePart;
  }

  @NotNull
  @Override
  public String getMaxPossibleText() {
    return MAX_POSSIBLE_TEXT;
  }

  @Override
  public float getAlignment() {
    return Component.LEFT_ALIGNMENT;
  }

  @Nullable
  @Override
  public String getTooltipText() {
    RevisionInfo revisionInfo = stateHolder.getBlame();
    if (revisionInfo.isEmpty()) {
      return null;
    } else {
      return blameStatusGateway.getCommitMessage(revisionInfo);
    }
  }

  @Nullable
  @Override
  public Consumer<MouseEvent> getClickConsumer() {
    return event -> {
      Editor editor = stateHolder.getCurrentEditor();
      VirtualFile currentFile = stateHolder.getCurrentFile();
      RevisionInfo revisionInfo = stateHolder.getBlame();
      if (editor != null && currentFile != null && revisionInfo.isNotEmpty()) {
        BlameUi.showBlamePopup(editor, currentFile, revisionInfo);
      }
    };
  }

  @Override
  public void setVisible(boolean visible) {
    boolean oldVisible = this.visible;
    this.visible = visible;
    boolean updated = false;
    if (shouldShow()) {
      VirtualFile currentFile = getCurrentFileUnderVcs();
      if (currentFile == null) {
        Editor editor = getEditor();
        stateHolder.updateCurrentEditor(editor);
        if (editor != null) {
          updateForEditor(editor);
        } else {
          updated = clearBlame();
          if (oldVisible != visible) {
            updated = true;
          }
        }
      } else {
        fileChanged(stateHolder.getCurrentEditor(), currentFile);
      }
    } else {
      if (visible) {
        updated = clearBlame();
      } else {
        disabled();
      }
    }
    if (updated) {
      updateWidget();
    }
  }

  private boolean shouldShow() {
    return visible && !DumbService.isDumb(myProject);
  }

  private void updateWidget() {
    if (myStatusBar != null) {
      myStatusBar.updateWidget(ID());
    }
  }

  @Override
  public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    if (shouldShow()) {
      VirtualFile currentFile = stateHolder.getCurrentFile();
      if (!file.equals(currentFile) && blameStatusGateway.isUnderVcs(file)) {
        Editor selectedEditor = source.getSelectedTextEditor();
        stateHolder.updateCurrentEditor(selectedEditor);
        fileChanged(selectedEditor, file);
      }
    }
  }

  @Override
  public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    BlameService.getInstance(myProject).fileClosed(file);
    if (clearBlame()) {
      updateWidget();
    }
  }

  @Override
  public void selectionChanged(@NotNull FileEditorManagerEvent event) {
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      updateForSelectionTimer.time(() -> {
        Editor editor = stateHolder.updateCurrentEditor(getEditor());
        stateHolder.updateCurrentFile(event.getNewFile());
        if (editor != null) {
          updateForEditorWithCurrentFile(editor);
        }
      });
    }
  }

  private void fileChanged(@Nullable Editor editor, @NotNull VirtualFile file) {
    RevisionInfo revisionInfo = RevisionInfo.EMPTY;
    if (editor != null) {
      int currentLine = BlameUi.getCurrentLineIndex(editor);
      if (BlameUi.isValidLineIndex(currentLine)) {
        revisionInfo = BlameService.getInstance(myProject).getDocumentLineIndexBlame(editor.getDocument(), file,
            currentLine);
      }
    } else {
      revisionInfo = BlameService.getInstance(myProject).getFileBlame(file);
    }
    if (updateBlame(revisionInfo)) {
      updateWidget();
    }
  }

  private boolean updateBlame(@NotNull RevisionInfo revisionInfo) {
    if (revisionInfo.isEmpty()) {
      return clearBlame();
    } else {
      if (stateHolder.updateBlame(revisionInfo)) {
        blameText = BlamePresenter.getInstance().getStatusBar(revisionInfo);
        return true;
      }
      return false;
    }
  }

  private boolean clearBlame() {
    if (stateHolder.clearBlame()) {
      blameText = ResBundle.message("blame.prefix") + " " + ResBundle.na();
      return true;
    }
    return false;
  }

  private void disabled() {
    blameText = ResBundle.message("blame.prefix") + " " + ResBundle.disabled();
  }

  @Override
  public void dispose() {
    super.dispose();
    blameStatusGateway.removeDumbModeExitAction(blameDumbModeExitAction);
    blameStatusGateway.removeBulkUpdateFinishedAction(bulkUpdateFinishedAction);
    blameStatusGateway.removeBlameUpdateAction(blameUpdatedAction);
  }
}

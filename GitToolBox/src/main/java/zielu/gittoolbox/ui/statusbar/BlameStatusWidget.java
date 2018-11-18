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
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import java.awt.Component;
import java.awt.event.MouseEvent;
import javax.swing.JTextArea;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.blame.Blame;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.MetricsHost;

public class BlameStatusWidget extends EditorBasedWidget implements StatusBarUi,
    StatusBarWidget.Multiframe, StatusBarWidget.TextPresentation {

  private static final String ID = BlameStatusWidget.class.getName();
  private static final int MAX_LENGTH = 27;
  private static final String MAX_POSSIBLE_TEXT = Strings.repeat("0", MAX_LENGTH);
  private final BlameService lens;
  private final Timer updateForDocumentTimer;
  private final Timer updateForCaretTimer;
  private final Timer updateForSelectionTimer;
  private final BlameStatusUi blameStatusUi;
  private final BlameStateHolder stateHolder;
  private final Runnable blameDumbModeExitAction;
  private final Consumer<Document> bulkUpdateFinishedAction;
  private String blameText = ResBundle.na();
  private String blameDetails;
  private boolean visible;

  public BlameStatusWidget(@NotNull Project project) {
    super(project);
    stateHolder = new BlameStateHolder();
    Metrics metrics = MetricsHost.project(project);
    updateForDocumentTimer = metrics.timer("blame-statusbar-update-for-document");
    updateForCaretTimer = metrics.timer("blame-statusbar-update-for-caret");
    updateForSelectionTimer = metrics.timer("blame-statusbar-update-for-selection");
    lens = BlameService.getInstance(project);
    clearBlame();
    blameDumbModeExitAction = () -> {
      Editor editor = getEditor();
      if (editor != null) {
        updateForEditor(editor);
      }
    };
    bulkUpdateFinishedAction = this::updateForDocument;
    blameStatusUi = BlameStatusUi.getInstance(project);
    blameStatusUi.addDumbModeExitAction(blameDumbModeExitAction);
    blameStatusUi.addBulkUpdateFinishedAction(bulkUpdateFinishedAction);
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
      }
    }
  }

  private boolean isDocumentInvalid(@Nullable Document document) {
    if (blameStatusUi.isInBulkUpdate(document)) {
      return true;
    }
    return stateHolder.isCurrentEditorDocument(document);
  }

  @Nullable
  private VirtualFile getCurrentFileUnderVcs() {
    VirtualFile currentFile = stateHolder.getCurrentFile();
    if (currentFile != null && blameStatusUi.isUnderVcs(currentFile)) {
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
    if (shouldShow()) {
      VirtualFile currentFile = getCurrentFileUnderVcs();
      if (currentFile != null) {
        fileChanged(selectedEditor, currentFile);
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
    return blameDetails;
  }

  @Nullable
  @Override
  public Consumer<MouseEvent> getClickConsumer() {
    return event -> {
      Editor editor = stateHolder.getCurrentEditor();
      if (blameDetails != null && editor != null) {
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
      if (!file.equals(currentFile) && blameStatusUi.isUnderVcs(file)) {
        Editor selectedEditor = source.getSelectedTextEditor();
        stateHolder.updateCurrentEditor(selectedEditor);
        fileChanged(selectedEditor, file);
      }
    }
  }

  @Override
  public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    lens.fileClosed(file);
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
        if (shouldShow()) {
          VirtualFile currentFile = getCurrentFileUnderVcs();
          if (currentFile != null) {
            fileChanged(editor, currentFile);
          }
        }
      });
    }
  }

  private void fileChanged(@Nullable Editor editor, @NotNull VirtualFile file) {
    Blame blame;
    if (editor != null) {
      blame = lens.getCurrentLineBlame(editor, file);
    } else {
      blame = lens.getFileBlame(file);
    }
    if (updateBlame(blame)) {
      updateWidget();
    }
  }

  private boolean updateBlame(@Nullable Blame blame) {
    if (blame != null) {
      if (stateHolder.updateBlame(blame)) {
        blameText = blame.getShortStatus();
        blameDetails = blame.getDetailedText();
        return true;
      }
      return false;
    } else {
      return clearBlame();
    }
  }

  private boolean clearBlame() {
    if (stateHolder.clearBlame()) {
      blameText = ResBundle.getString("blame.prefix") + " " + ResBundle.na();
      blameDetails = null;
      return true;
    }
    return false;
  }

  private void disabled() {
    blameText = ResBundle.getString("blame.prefix") + " " + ResBundle.disabled();
    blameDetails = null;
  }

  @Override
  public void dispose() {
    super.dispose();
    blameStatusUi.removeDumbModeExitAction(blameDumbModeExitAction);
    blameStatusUi.removeBulkUpdateFinishedAction(bulkUpdateFinishedAction);
  }
}

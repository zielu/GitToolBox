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
import com.intellij.openapi.fileEditor.FileDocumentManager;
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
import git4idea.GitVcs;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JTextArea;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.blame.Blame;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.MetricsHost;
import zielu.gittoolbox.util.GtUtil;

public class BlameStatusWidget extends EditorBasedWidget implements StatusBarWidget.Multiframe,
    StatusBarWidget.TextPresentation {
  private static final String ID = BlameStatusWidget.class.getName();
  private static final int MAX_LENGTH = 27;
  private static final String PREFIX = ResBundle.getString("blame.prefix");
  private static final int BLAME_LENGTH = MAX_LENGTH - PREFIX.length() - 1;
  private static final String MAX_POSSIBLE_TEXT = Strings.repeat("0", MAX_LENGTH);
  private final GitVcs git;
  private final BlameService lens;
  private final Timer updateForDocumentTimer;
  private final Timer updateForCaretTimer;
  private final Timer updateForSelectionTimer;
  private final Timer underVcsTimer;
  // store editor here to avoid expensive and EDT-only getSelectedEditor() retrievals
  private volatile Reference<Editor> editor = new WeakReference<>(null);
  private volatile Reference<VirtualFile> file = new WeakReference<>(null);
  private String blameText = ResBundle.na();
  private String blameDetails;
  private boolean visible;

  public BlameStatusWidget(@NotNull Project project) {
    super(project);
    git = GitVcs.getInstance(project);
    Metrics metrics = MetricsHost.project(project);
    updateForDocumentTimer = metrics.timer("blame-statusbar-update-for-document");
    updateForCaretTimer = metrics.timer("blame-statusbar-update-for-caret");
    updateForSelectionTimer = metrics.timer("blame-statusbar-update-for-selection");
    underVcsTimer = metrics.timer("blame-statusbar-under-vcs");
    lens = BlameService.getInstance(project);
    clearBlame();
    myConnection.subscribe(DumbService.DUMB_MODE, new DumbService.DumbModeListener() {
      @Override
      public void exitDumbMode() {
        Editor editor = getEditor();
        if (editor != null) {
          updateForEditor(editor);
        }
      }
    });
  }

  @Override
  public void install(@NotNull StatusBar statusBar) {
    super.install(statusBar);
    EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();
    eventMulticaster.addDocumentListener(new DocumentListener() {
      @Override
      public void documentChanged(DocumentEvent e) {
        Document document = e.getDocument();
        updateForDocumentTimer.time(() -> updateForDocument(document));
      }
    }, this);
    eventMulticaster.addCaretListener(new CaretListener() {
      @Override
      public void caretPositionChanged(CaretEvent e) {
        Editor editor = e.getEditor();
        updateForCaretTimer.time(() -> updateForEditor(editor));
      }
    }, this);
  }

  private void updateForDocument(@Nullable Document document) {
    Editor selectedEditor = editor.get();
    if (document != null && (selectedEditor == null || selectedEditor.getDocument() != document)) {
      return;
    }
    if (document != null) {
      file = new WeakReference<>(FileDocumentManager.getInstance().getFile(document));
    } else {
      file = new WeakReference<>(null);
    }
    if (shouldShow()) {
      VirtualFile currentFile = getCurrentFileUnderVcs();
      if (currentFile != null) {
        fileChanged(selectedEditor, currentFile);
      }
    }
  }

  @Nullable
  private VirtualFile getCurrentFileUnderVcs() {
    VirtualFile currentFile = file.get();
    if (currentFile != null && isUnderVcs(currentFile)) {
      return currentFile;
    }
    return null;
  }

  private boolean isUnderVcs(@NotNull VirtualFile file) {
    return underVcsTimer.timeSupplier(() -> git.fileIsUnderVcs(GtUtil.localFilePath(file)));
  }

  private void updateForEditor(@NotNull Editor updatedEditor) {
    Editor selectedEditor = editor.get();
    if (selectedEditor == null || selectedEditor.getDocument() != updatedEditor.getDocument()) {
      return;
    }
    file = new WeakReference<>(FileDocumentManager.getInstance().getFile(updatedEditor.getDocument()));
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
    String blamePart = Ascii.truncate(blameText, BLAME_LENGTH, "...");
    blamePart = Strings.padEnd(blamePart, BLAME_LENGTH, ' ');
    return PREFIX + " " + blamePart;
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
      Editor editor = this.editor.get();
      if (blameDetails != null && editor != null) {
        JTextArea content = new JTextArea(blameDetails);
        content.setEditable(false);
        JBPopupFactory.getInstance()
            .createDialogBalloonBuilder(content, ResBundle.getString("statusBar.blame.popup.title"))
            .setDialogMode(true)
            .setCloseButtonEnabled(false)
            .setHideOnClickOutside(true)
            .createBalloon().showInCenterOf(editor.getComponent());
      }
    };
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
    if (shouldShow()) {
      VirtualFile currentFile = getCurrentFileUnderVcs();
      if (currentFile != null) {
        fileChanged(editor.get(), currentFile);
      } else {
        clearBlame();
      }
    } else {
      clearBlame();
    }
    updateWidget();
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
      if (!file.equals(this.file.get()) && isUnderVcs(file)) {
        Editor selectedEditor = source.getSelectedTextEditor();
        editor = new WeakReference<>(selectedEditor);
        fileChanged(selectedEditor, file);
      }
    }
  }

  @Override
  public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    lens.fileClosed(file);
    clearBlame();
    updateWidget();
  }

  @Override
  public void selectionChanged(@NotNull FileEditorManagerEvent event) {
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      updateForSelectionTimer.time(() -> {
        editor = new WeakReference<>(getEditor());
        file = new WeakReference<>(event.getNewFile());
        if (shouldShow()) {
          VirtualFile currentFile = getCurrentFileUnderVcs();
          if (currentFile != null) {
            fileChanged(editor.get(), currentFile);
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
    updateBlame(blame);
    updateWidget();
  }

  private void updateBlame(@Nullable Blame blame) {
    if (blame != null) {
      blameText = blame.getShortText();
      blameDetails = blame.getDetailedText();
    } else {
      clearBlame();
    }
  }

  private void clearBlame() {
    blameText = ResBundle.na();
    blameDetails = null;
  }
}

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
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JTextArea;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.lens.Blame;
import zielu.gittoolbox.lens.BlameService;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.MetricsHost;

public class BlameStatusWidget extends EditorBasedWidget implements StatusBarWidget.Multiframe,
    StatusBarWidget.TextPresentation {
  private static final String ID = BlameStatusWidget.class.getName();
  private static final String MAX_POSSIBLE_TEXT = Strings.repeat("0", 27);
  private final BlameService lens;
  private final Timer updateForDocumentTimer;
  private final Timer updateForCaretTimer;
  private final Timer updateForSelectionTimer;
  // store editor here to avoid expensive and EDT-only getSelectedEditor() retrievals
  private volatile Reference<Editor> editor = new WeakReference<>(null);
  private volatile Reference<VirtualFile> file = new WeakReference<>(null);
  private String blameText = ResBundle.na();
  private String blameDetails;
  private boolean visible;

  public BlameStatusWidget(@NotNull Project project) {
    super(project);
    Metrics metrics = MetricsHost.project(project);
    updateForDocumentTimer = metrics.timer("lens-statusbar-update-for-document");
    updateForCaretTimer = metrics.timer("lens-statusbar-update-for-caret");
    updateForSelectionTimer = metrics.timer("lens-statusbar-update-for-selection");
    lens = BlameService.getInstance(project);
    clearBlame();
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
    VirtualFile currentFile = file.get();
    if (currentFile != null) {
      fileChanged(selectedEditor, currentFile);
    }
  }

  private void updateForEditor(@NotNull Editor updatedEditor) {
    Editor selectedEditor = editor.get();
    if (selectedEditor == null || selectedEditor.getDocument() != updatedEditor.getDocument()) {
      return;
    }
    file = new WeakReference<>(FileDocumentManager.getInstance().getFile(updatedEditor.getDocument()));
    VirtualFile currentFile = file.get();
    if (currentFile != null) {
      fileChanged(selectedEditor, currentFile);
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
    return "Blame: " + Ascii.truncate(blameText, 20, "...");
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
    Editor editor = this.editor.get();
    if (blameDetails == null || editor == null) {
      return null;
    } else {
      return event -> {
        JTextArea content = new JTextArea(blameDetails);
        content.setEditable(false);
        JBPopupFactory.getInstance()
            .createDialogBalloonBuilder(content, "Blame")
            .setDialogMode(true)
            .setCloseButtonEnabled(false)
            .setHideOnClickOutside(true)
            .createBalloon().showInCenterOf(editor.getComponent());
      };
    }
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
    if (shouldShow()) {
      VirtualFile currentFile = file.get();
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
      if (!file.equals(this.file.get())) {
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
          VirtualFile currentFile = file.get();
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

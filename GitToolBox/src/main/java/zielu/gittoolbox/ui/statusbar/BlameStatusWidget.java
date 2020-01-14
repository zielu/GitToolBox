package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.blame.BlameListener;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.config.AppConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.ui.blame.BlameUi;
import zielu.gittoolbox.ui.blame.BlameUiService;
import zielu.gittoolbox.ui.util.AppUiUtil;

class BlameStatusWidget extends EditorBasedWidget implements StatusBarUi, StatusBarWidget.TextPresentation {
  public static final String ID = BlameStatusWidget.class.getName();

  private final AtomicBoolean visible = new AtomicBoolean();
  private final AtomicBoolean connected = new AtomicBoolean();
  private final CaretListener caretListener = new CaretListener() {
    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
      onCaretPositionChanged(event);
    }
  };
  private String text = "";

  BlameStatusWidget(@NotNull Project project) {
    super(project);
  }

  private void onCaretPositionChanged(@NotNull CaretEvent caretEvent) {
    if (shouldShow() && Objects.equals(myProject, caretEvent.getEditor().getProject())) {
      Caret caret = caretEvent.getCaret();
      if (caret == null || caret.isUpToDate()) {
        int lineIndex = caretEvent.getNewPosition().line;
        VirtualFile selectedFile = getSelectedFile();
        if (selectedFile != null) {
          updateStatus(selectedFile, lineIndex);
        }
      }
    }
  }

  private boolean shouldShow() {
    return visible.get() && !isDisposed() && !DumbService.isDumb(myProject);
  }

  private void updateStatus(@NotNull VirtualFile file, int lineIndex) {
    String status = BlameUiService.getInstance(myProject).getBlameStatus(file, lineIndex);
    updatePresentation(status);
  }

  private void updatePresentation(@Nullable String status) {
    if (status != null) {
      text = status;
    } else {
      text = "";
    }
    repaintStatusBar();
  }

  private void repaintStatusBar() {
    AppUiUtil.invokeLaterIfNeeded(myProject, () -> myStatusBar.updateWidget(ID));
  }

  @NotNull
  @Override
  public String ID() {
    return ID;
  }

  @Nullable
  @Override
  public WidgetPresentation getPresentation() {
    return this;
  }

  private void setVisible(boolean visible) {
    if (visible && this.visible.compareAndSet(false, true)) {
      EditorFactory.getInstance().getEventMulticaster().addCaretListener(caretListener, myProject);
      AppUiUtil.invokeLater(myProject, this::updateBlame);
    } else if (!visible && this.visible.compareAndSet(true, false)) {
      EditorFactory.getInstance().getEventMulticaster().removeCaretListener(caretListener);
    }
  }

  private void initialize() {
    if (connected.compareAndSet(false, true)) {
      connect();
    }
  }

  private void connect() {
    myConnection.subscribe(BlameService.BLAME_UPDATE, new BlameListener() {
      @Override
      public void blameUpdated(@NotNull VirtualFile file) {
        if (shouldShow()) {
          AppUiUtil.invokeLaterIfNeeded(myProject, () -> updateBlame(file));
        }
      }

      @Override
      public void blameInvalidated(@NotNull VirtualFile file) {
        if (shouldShow()) {
          AppUiUtil.invokeLaterIfNeeded(myProject, () -> updatePresentation(null));
        }
      }
    });
    myConnection.subscribe(AppConfigNotifier.CONFIG_TOPIC, new AppConfigNotifier() {
      @Override
      public void configChanged(@NotNull GitToolBoxConfig2 previous, @NotNull GitToolBoxConfig2 current) {
        updateVisibleFromConfig();
        if (shouldShow() && current.isBlameStatusPresentationChanged(previous)) {
          AppUiUtil.invokeLaterIfNeeded(myProject, BlameStatusWidget.this::updateBlame);
        }
        if (current.isShowBlameWidgetChanged(previous.showBlameWidget)) {
          repaintStatusBar();
        }
      }
    });
    myConnection.subscribe(DumbService.DUMB_MODE, new DumbService.DumbModeListener() {
      @Override
      public void exitDumbMode() {
        if (shouldShow()) {
          AppUiUtil.invokeLaterIfNeeded(myProject, BlameStatusWidget.this::updateBlame);
        }
      }
    });
  }

  private void updateBlame() {
    VirtualFile selectedFile = getSelectedFile();
    if (selectedFile != null) {
      updateBlame(selectedFile);
    }
  }

  private void updateBlame(@NotNull VirtualFile file) {
    if (Objects.equals(getSelectedFile(), file)) {
      int lineIndex = BlameUi.getCurrentLineIndex(getEditor());
      if (BlameUi.isValidLineIndex(lineIndex)) {
        updateStatus(file, lineIndex);
      }
    }
  }

  @NotNull
  @Override
  public String getText() {
    if (visible.get()) {
      return text;
    }
    return "";
  }

  @Override
  public float getAlignment() {
    return Component.LEFT_ALIGNMENT;
  }

  @Nullable
  @Override
  public String getTooltipText() {
    if (visible.get()) {
      VirtualFile selectedFile = getSelectedFile();
      if (selectedFile != null) {
        int lineIndex = BlameUi.getCurrentLineIndex(getEditor());
        if (BlameUi.isValidLineIndex(lineIndex)) {
          return BlameUiService.getInstance(myProject).getBlameStatusTooltip(selectedFile, lineIndex);
        }
      }
    }
    return null;
  }

  @Override
  public void install(@NotNull StatusBar statusBar) {
    super.install(statusBar);
    initialize();
    updateVisibleFromConfig();
  }

  private void updateVisibleFromConfig() {
    setVisible(GitToolBoxConfig2.getInstance().showBlameWidget);
  }

  @Override
  public void dispose() {
    setVisible(false);
    super.dispose();
  }

  @Nullable
  @Override
  public Consumer<MouseEvent> getClickConsumer() {
    if (visible.get()) {
      return this::showPopup;
    } else {
      return null;
    }
  }

  private void showPopup(MouseEvent event) {
    VirtualFile selectedFile = getSelectedFile();
    Editor editor = getEditor();
    if (selectedFile != null && editor != null) {
      int lineIndex = BlameUi.getCurrentLineIndex(getEditor());
      if (BlameUi.isValidLineIndex(lineIndex)) {
        RevisionInfo revisionInfo = BlameService.getInstance(myProject)
            .getDocumentLineIndexBlame(editor.getDocument(), selectedFile, lineIndex);
        if (revisionInfo.isNotEmpty()) {
          BlameUi.showBlamePopup(editor, selectedFile, revisionInfo);
        }
      }
    }
  }
}

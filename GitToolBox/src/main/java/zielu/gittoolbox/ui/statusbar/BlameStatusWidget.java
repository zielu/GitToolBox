package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
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
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.ui.blame.BlameUiService;
import zielu.gittoolbox.ui.util.AppUiUtil;

class BlameStatusWidget extends EditorBasedWidget implements StatusBarUi, StatusBarWidget.TextPresentation {
  private static final String ID = BlameStatusWidget.class.getName();

  private final AtomicBoolean visible = new AtomicBoolean();
  private final CaretListener caretListener = new CaretListener() {
    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
      onCaretPositionChanged(event);
    }
  };
  private BlameUiService uiService;
  private String text = "";
  private String tooltip;

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
    String status = uiService.getBlameStatus(file, lineIndex);
    updatePresentation(status);
  }

  private void updatePresentation(@Nullable String status) {
    if (status != null) {
      text = status;
    } else {
      text = "";
    }
    myStatusBar.updateWidget(ID);
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

  @Override
  public void setVisible(boolean visible) {
    if (visible && this.visible.compareAndSet(false, true)) {
      uiService = BlameUiService.getInstance(myProject);
      EditorFactory.getInstance().getEventMulticaster().addCaretListener(caretListener);
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
            updatePresentation(null);
          }
        }
      });
      myConnection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier() {
        @Override
        public void configChanged(GitToolBoxConfig2 previous, GitToolBoxConfig2 current) {
          if (shouldShow() && current.isBlameStatusPresentationChanged(previous)) {
            updateBlame();
          }
        }
      });
      AppUiUtil.invokeLater(myProject, this::updateBlame);
    } else if (!visible && this.visible.compareAndSet(true, false)) {
      EditorFactory.getInstance().getEventMulticaster().removeCaretListener(caretListener);
    }
  }

  private void updateBlame() {
    VirtualFile selectedFile = getSelectedFile();
    if (selectedFile != null) {
      updateBlame(selectedFile);
    }
  }

  private void updateBlame(@NotNull VirtualFile file) {
    Editor editor = getEditor();
    if (editor != null && Objects.equals(getSelectedFile(), file)) {
      CaretModel caretModel = editor.getCaretModel();
      int lineIndex = caretModel.getLogicalPosition().line;
      updateStatus(file, lineIndex);
    }
  }

  @NotNull
  @Override
  public String getText() {
    return text;
  }

  @Override
  public float getAlignment() {
    return Component.LEFT_ALIGNMENT;
  }

  @Nullable
  @Override
  public String getTooltipText() {
    return tooltip;
  }

  @Nullable
  @Override
  public Consumer<MouseEvent> getClickConsumer() {
    return null;
  }
}

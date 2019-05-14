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
import com.intellij.util.messages.MessageBusConnection;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.blame.BlameListener;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.ui.blame.BlameUiService;
import zielu.gittoolbox.ui.util.AppUiUtil;

class BlameStatusWidget extends EditorBasedWidget implements StatusBarUi, StatusBarWidget.TextPresentation {
  private static final String ID = BlameStatusWidget.class.getName();

  private final CaretListener caretListener = new CaretListener() {
    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
      onCaretPositionChanged(event);
    }
  };
  private BlameUiService uiService;
  private MessageBusConnection connection;
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
    return !DumbService.isDumb(myProject);
  }

  private void updateStatus(@NotNull VirtualFile file, int lineIndex) {
    String status = uiService.getBlameStatus(file, lineIndex);
    update(status);
  }

  private void update(@Nullable String status) {
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
    if (visible) {
      uiService = BlameUiService.getInstance(myProject);
      EditorFactory.getInstance().getEventMulticaster().addCaretListener(caretListener);
      connection = myProject.getMessageBus().connect();
      connection.subscribe(BlameService.BLAME_UPDATE, new BlameListener() {
        @Override
        public void blameUpdated(@NotNull VirtualFile file) {
          if (shouldShow()) {
            AppUiUtil.invokeLaterIfNeeded(myProject, () -> onBlameUpdated(file));
          }
        }

        @Override
        public void blameInvalidated(@NotNull VirtualFile file) {
          update(null);
        }
      });
    } else {
      EditorFactory.getInstance().getEventMulticaster().removeCaretListener(caretListener);
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private void onBlameUpdated(@NotNull VirtualFile file) {
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

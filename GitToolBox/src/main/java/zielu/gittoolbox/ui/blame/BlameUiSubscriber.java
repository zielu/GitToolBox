package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.blame.BlameAnnotation;
import zielu.gittoolbox.blame.BlameCache;
import zielu.gittoolbox.blame.BlameCacheListener;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfig2;

class BlameUiSubscriber implements BaseComponent {
  private final Logger log = Logger.getInstance(getClass());
  private MessageBusConnection connection;

  BlameUiSubscriber(@NotNull Project project) {
    connection = project.getMessageBus().connect();
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier.Adapter() {
      @Override
      public void configChanged(GitToolBoxConfig2 previous, GitToolBoxConfig2 current) {
        if (current.showBlame != previous.showBlame
            || current.showEditorInlineBlame != previous.showEditorInlineBlame) {
          VirtualFile file = getFileForSelectedEditor(project);
          if (file != null) {
            log.debug("Refresh editor on config change for ", file);
            refreshEditorFile(project, file);
          }
        }
      }
    });
    connection.subscribe(BlameCache.TOPIC, new BlameCacheListener() {
      @Override
      public void cacheUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
        GitToolBoxConfig2 config = GitToolBoxConfig2.getInstance();
        if (config.showBlame && config.showEditorInlineBlame) {
          VirtualFile fileInEditor = getFileForSelectedEditor(project);
          if (Objects.equals(fileInEditor, file)) {
            log.debug("Refresh editor on blame update for ", file);
            refreshEditorFile(project, file);
          }
        }
      }
    });
  }

  @Nullable
  private VirtualFile getFileForSelectedEditor(@NotNull Project project) {
    FileEditor editor = FileEditorManager.getInstance(project).getSelectedEditor();
    if (editor != null) {
      return editor.getFile();
    }
    return null;
  }

  private void refreshEditorFile(@NotNull Project project, @NotNull VirtualFile file) {
    FileEditorManagerEx.getInstanceEx(project).updateFilePresentation(file);
  }

  @Override
  public void disposeComponent() {
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
  }
}

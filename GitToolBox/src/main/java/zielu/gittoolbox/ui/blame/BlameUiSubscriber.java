package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfig2;

class BlameUiSubscriber implements Disposable {
  private MessageBusConnection connection;

  BlameUiSubscriber(@NotNull Project project) {
    connection = project.getMessageBus().connect();
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier.Adapter() {
      @Override
      public void configChanged(GitToolBoxConfig2 previous, GitToolBoxConfig2 current) {
        if (current.showBlame != previous.showBlame
            || current.showEditorInlineBlame != previous.showEditorInlineBlame) {
          FileEditor editor = FileEditorManager.getInstance(project).getSelectedEditor();
          if (editor != null) {
            VirtualFile file = editor.getFile();
            if (file != null) {
              FileEditorManagerEx.getInstanceEx(project).updateFilePresentation(file);
            }
          }
        }
      }
    });
  }

  @Override
  public void dispose() {
    if (connection != null) {
      connection.disconnect();
    }
  }
}

package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.blame.BlameListener;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.ui.util.AppUiUtil;

class BlameUiSubscriber {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;

  BlameUiSubscriber(@NotNull Project project) {
    this.project = project;
    MessageBusConnection connection = project.getMessageBus().connect(project);
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier() {
      @Override
      public void configChanged(GitToolBoxConfig2 previous, GitToolBoxConfig2 current) {
        if (onConfigChanged(previous, current)) {
          AppUiUtil.invokeLater(project, () -> handleConfigChanged());
        }
      }
    });
    connection.subscribe(BlameService.BLAME_UPDATE, new BlameListener() {
      @Override
      public void blameUpdated(@NotNull VirtualFile file) {
        onBlameUpdate(file);
      }

      @Override
      public void blameInvalidated(@NotNull VirtualFile file) {
        onBlameUpdate(file);
      }
    });
    connection.subscribe(EditorColorsManager.TOPIC, this::onColorSchemeChanged);
    connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
      @Override
      public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        onFileClosed(file);
      }
    });
  }

  private void onFileClosed(@NotNull VirtualFile file) {
    BlameService.getExistingInstance(project).ifPresent(sevice -> sevice.fileClosed(file));
  }

  private void handleConfigChanged() {
    VirtualFile file = getFileForSelectedEditor();
    if (file != null) {
      log.debug("Refresh editor on config change for ", file);
      refreshEditorFile(file);
    }
  }

  private void onBlameUpdate(@NotNull VirtualFile file) {
    log.debug("Blame updated: ", file);
    GitToolBoxConfig2 config = GitToolBoxConfig2.getInstance();
    if (config.showEditorInlineBlame) {
      BlameUiService.getExistingInstance(project).ifPresent(service -> service.blameUpdated(file));
      AppUiUtil.invokeLaterIfNeeded(project, () -> handleBlameUpdate(file));
    }
  }

  private void handleBlameUpdate(@NotNull VirtualFile file) {
    if (FileEditorManager.getInstance(project).isFileOpen(file)) {
      log.debug("Refresh editors on blame update for ", file);
      refreshEditorFile(file, true);
    }
  }

  @Nullable
  private VirtualFile getFileForSelectedEditor() {
    FileEditor editor = FileEditorManager.getInstance(project).getSelectedEditor();
    if (editor != null) {
      return editor.getFile();
    }
    return null;
  }

  private void refreshEditorFile(@NotNull VirtualFile file) {
    refreshEditorFile(file, false);
  }

  private void refreshEditorFile(@NotNull VirtualFile file, boolean repaint) {
    FileEditorManagerEx editorManagerEx = FileEditorManagerEx.getInstanceEx(project);
    editorManagerEx.updateFilePresentation(file);
    /*if (repaint) {
      for (FileEditor editor : editorManagerEx.getEditors(file)) {
        if (editor instanceof TextEditor) {
          TextEditor textEditor = (TextEditor) editor;
          textEditor.getEditor().getContentComponent().repaint();
        }
      }
    }*/
  }

  private void onColorSchemeChanged(@Nullable EditorColorsScheme scheme) {
    if (scheme != null) {
      BlameUiService.getExistingInstance(project).ifPresent(service -> service.colorsSchemeChanged(scheme));
    }
  }

  private boolean onConfigChanged(GitToolBoxConfig2 previous, GitToolBoxConfig2 current) {
    boolean blamePresentationChanged = current.isBlameInlinePresentationChanged(previous);
    BlameUiService.getExistingInstance(project).ifPresent(service -> service.configChanged(previous, current));
    return current.showBlameWidget != previous.showBlameWidget
        || current.showEditorInlineBlame != previous.showEditorInlineBlame
        || blamePresentationChanged;
  }
}

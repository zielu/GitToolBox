package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.config.AppConfig;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.ui.util.AppUiUtil;
import zielu.gittoolbox.util.AppUtil;

class BlameUiSubscriber implements Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;

  BlameUiSubscriber(@NotNull Project project) {
    this.project = project;
  }

  static BlameUiSubscriber getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, BlameUiSubscriber.class);
  }

  void onFileClosed(@NotNull VirtualFile file) {
    BlameService.getExistingInstance(project).ifPresent(service -> service.fileClosed(file));
  }

  void onBlameUpdate(@NotNull VirtualFile file) {
    log.debug("Blame updated: ", file);
    GitToolBoxConfig2 config = AppConfig.getConfig();
    if (config.getShowEditorInlineBlame()) {
      BlameUiService.getExistingInstance(project).ifPresent(service -> service.blameUpdated(file));
      AppUiUtil.invokeLaterIfNeeded(this, () -> handleBlameUpdate(file));
    }
  }

  private void handleBlameUpdate(@NotNull VirtualFile file) {
    if (FileEditorManager.getInstance(project).isFileOpen(file)) {
      log.debug("Refresh editors on blame update for ", file);
      refreshEditorFile(file);
    }
  }

  private void refreshEditorFile(@NotNull VirtualFile file) {
    FileEditorManagerEx editorManagerEx = FileEditorManagerEx.getInstanceEx(project);
    for (FileEditor editor : editorManagerEx.getEditors(file)) {
      editor.getComponent().repaint();
    }
  }

  void onColorSchemeChanged(@NotNull EditorColorsScheme scheme) {
    BlameUiService.getExistingInstance(project).ifPresent(service -> service.colorsSchemeChanged(scheme));
  }

  void onConfigChanged(@NotNull GitToolBoxConfig2 previous, @NotNull GitToolBoxConfig2 current) {
    if (handleConfigChanged(previous, current)) {
      AppUiUtil.invokeLater(this, BlameUiSubscriber.this::handleConfigChanged);
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

  private void handleConfigChanged() {
    VirtualFile file = getFileForSelectedEditor();
    if (file != null) {
      log.debug("Refresh editor on config change for ", file);
      refreshEditorFile(file);
    }
  }

  private boolean handleConfigChanged(GitToolBoxConfig2 previous, GitToolBoxConfig2 current) {
    boolean blamePresentationChanged = current.isBlameInlinePresentationChanged(previous);
    BlameUiService.getExistingInstance(project).ifPresent(service -> service.configChanged(previous, current));
    return current.getShowBlameWidget() != previous.getShowBlameWidget()
        || current.getShowEditorInlineBlame() != previous.getShowEditorInlineBlame()
        || blamePresentationChanged;
  }

  @Override
  public void dispose() {
    //do nothing
  }
}

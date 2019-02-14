package zielu.gittoolbox.ui.blame;

import com.codahale.metrics.Timer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.FontUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.blame.Blame;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.config.DecorationColors;
import zielu.gittoolbox.metrics.ProjectMetrics;

class DefaultBlameEditorService implements BlameEditorService {
  private final Project project;
  private final Timer blameEditorTimer;

  DefaultBlameEditorService(@NotNull Project project, @NotNull ProjectMetrics metrics) {
    this.project = project;
    this.blameEditorTimer = metrics.timer("blame-editor-painter");
  }

  @Nullable
  @Override
  public Collection<LineExtensionInfo> getLineExtensions(@NotNull VirtualFile file, int editorLineNumber) {
    return blameEditorTimer.timeSupplier(() -> getLineAnnotation(file, editorLineNumber));
  }

  @Nullable
  private Collection<LineExtensionInfo> getLineAnnotation(@NotNull VirtualFile file, int editorLineNumber) {
    VirtualFileRepoCache fileRepoCache = VirtualFileRepoCache.getInstance(project);
    if (fileRepoCache.isUnderGitRoot(file)) {
      Document document = FileDocumentManager.getInstance().getDocument(file);
      if (document != null) {
        if (isLineWithCaret(document, editorLineNumber)) {
          Blame lineBlame = getLineBlame(document, file, editorLineNumber);
          if (lineBlame != null) {
            return getDecoration(lineBlame);
          }
        }
      }
    }
    return null;
  }

  private boolean isLineWithCaret(@NotNull Document document, int editorLineNumber) {

    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    if (editor != null && Objects.equals(editor.getDocument(), document)) {
      return BlameUi.getCurrentLineNumber(editor) == editorLineNumber;
    }
    return false;
  }

  private Blame getLineBlame(@NotNull Document document, @NotNull VirtualFile file, int editorLineNumber) {
    BlameService blameService = BlameService.getInstance(project);
    return blameService.getDocumentLineBlame(document, file, editorLineNumber);
  }

  private Collection<LineExtensionInfo> getDecoration(Blame blame) {
    String text = formatBlameText(blame);
    return Collections.singletonList(new LineExtensionInfo(text, getBlameTextAttributes()));
  }

  private String formatBlameText(Blame blame) {
    return new StringBand(3)
        .append(FontUtil.spaceAndThinSpace())
        .append(" ")
        .append(blame.getShortText())
        .toString();
  }

  private TextAttributes getBlameTextAttributes() {
    SimpleTextAttributes attributes = DecorationColors.simpleAttributes(
        DecorationColors.EDITOR_INLINE_BLAME_ATTRIBUTES);
    return attributes.toTextAttributes();
  }
}

package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
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
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.metrics.ProjectMetrics;

public class BlameEditorLinePainter extends EditorLinePainter {
  private static final String BLAME_PREFIX = FontUtil.spaceAndThinSpace() + " ";

  @Nullable
  @Override
  public Collection<LineExtensionInfo> getLineExtensions(@NotNull Project project, @NotNull VirtualFile file,
                                                         int editorLineNumber) {
    if (shouldShow(project)) {
      return ProjectMetrics.getInstance(project).timer("blame-editor-painter")
          .timeSupplier(() -> getLineAnnotation(project, file, editorLineNumber));
    }
    return null;
  }

  private boolean shouldShow(@NotNull Project project) {
    if (DumbService.isDumb(project)) {
      return false;
    } else {
      GitToolBoxConfig2 config = GitToolBoxConfig2.getInstance();
      return config.showBlame && config.showEditorInlineBlame;
    }
  }

  @Nullable
  private Collection<LineExtensionInfo> getLineAnnotation(@NotNull Project project, @NotNull VirtualFile file,
                                                          int editorLineNumber) {
    VirtualFileRepoCache fileRepoCache = VirtualFileRepoCache.getInstance(project);
    if (fileRepoCache.isUnderGitRoot(file)) {
      Document document = FileDocumentManager.getInstance().getDocument(file);
      if (document != null) {
        if (isLineWithCaret(project, document, editorLineNumber)) {
          Blame lineBlame = getLineBlame(project, document, file, editorLineNumber);
          if (lineBlame != null) {
            return getDecoration(lineBlame);
          }
        }
      }
    }
    return null;
  }

  private boolean isLineWithCaret(@NotNull Project project, @NotNull Document document, int editorLineNumber) {
    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    if (editor != null && Objects.equals(editor.getDocument(), document)) {
      return BlameUi.getCurrentLineNumber(editor) == editorLineNumber;
    }
    return false;
  }

  private Blame getLineBlame(@NotNull Project project, @NotNull Document document, @NotNull VirtualFile file,
                             int editorLineNumber) {
    BlameService blameService = BlameService.getInstance(project);
    return blameService.getDocumentLineBlame(document, file, editorLineNumber);
  }

  private Collection<LineExtensionInfo> getDecoration(Blame blame) {
    String text = formatBlameText(blame);
    return Collections.singletonList(new LineExtensionInfo(text, getBlameTextAttributes()));
  }

  private String formatBlameText(Blame blame) {
    return new StringBand(2)
        .append(BLAME_PREFIX)
        .append(BlamePresenter.getInstance().getEditorInline(blame))
        .toString();
  }

  private TextAttributes getBlameTextAttributes() {
    return DecorationColors.textAttributes(DecorationColors.EDITOR_INLINE_BLAME_ATTRIBUTES);
  }
}

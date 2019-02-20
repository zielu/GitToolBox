package zielu.gittoolbox.ui.blame;

import com.codahale.metrics.Timer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
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
import zielu.gittoolbox.metrics.ProjectMetrics;

class DefaultBlameEditorService implements BlameEditorService {
  private static final TextAttributesKey ATTRIBUTES_KEY = DecorationColors.EDITOR_INLINE_BLAME_ATTRIBUTES;
  private static final String BLAME_PREFIX = FontUtil.spaceAndThinSpace() + " ";
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;
  private final Timer blameEditorTimer;
  private TextAttributes blameTextAttributes;

  DefaultBlameEditorService(@NotNull Project project, @NotNull ProjectMetrics metrics) {
    this.project = project;
    this.blameEditorTimer = metrics.timer("blame-editor-painter");
    blameTextAttributes = DecorationColors.textAttributes(ATTRIBUTES_KEY);
  }

  @Override
  public void colorsSchemeChanged(@NotNull EditorColorsScheme colorsScheme) {
    blameTextAttributes = colorsScheme.getAttributes(ATTRIBUTES_KEY);
    log.debug("Color scheme updated");
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
        Editor editor = getEditor(document);
        if (editor != null && isLineWithCaret(editor, editorLineNumber)) {
          return getLineExtensionInfos(editor, document, file, editorLineNumber);
        }
      }
    }
    return null;
  }

  @Nullable
  private Collection<LineExtensionInfo> getLineExtensionInfos(@NotNull Editor editor, @NotNull Document document,
                                                              @NotNull VirtualFile file, int editorLineNumber) {
    Collection<LineExtensionInfo> cachedInfo = getOrClearCachedLineInfo(editor, editorLineNumber);
    if (cachedInfo != null) {
      return cachedInfo;
    } else {
      Blame lineBlame = getLineBlame(document, file, editorLineNumber);
      Collection<LineExtensionInfo> lineInfo = null;
      if (lineBlame.isNotEmpty()) {
        lineInfo = getDecoration(lineBlame);
      }
      cacheLineExtensionInfo(editor, editorLineNumber, lineInfo);
      return lineInfo;
    }
  }

  @Nullable
  private Editor getEditor(@NotNull Document document) {
    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    if (editor != null && Objects.equals(editor.getDocument(), document)) {
      return editor;
    }
    return null;
  }

  private boolean isLineWithCaret(@NotNull Editor editor, int editorLineNumber) {
    return BlameUi.getCurrentLineNumber(editor) == editorLineNumber;
  }

  @Nullable
  private Collection<LineExtensionInfo> getOrClearCachedLineInfo(@NotNull Editor editor, int editorLineNumber) {
    BlameEditorData editorData = BlameEditorData.KEY.get(editor);
    if (editorData != null) {
      if (editorData.isSameEditorLine(editorLineNumber)) {
        return editorData.getLineInfo();
      } else {
        BlameEditorData.KEY.set(editor, null);
      }
    }
    return null;
  }

  private void cacheLineExtensionInfo(@NotNull Editor editor, int editorLineNumber,
                                      @Nullable Collection<LineExtensionInfo> lineInfo) {
    BlameEditorData editorData = new BlameEditorData(editorLineNumber, lineInfo);
    BlameEditorData.KEY.set(editor, editorData);
  }

  @NotNull
  private Blame getLineBlame(@NotNull Document document, @NotNull VirtualFile file, int editorLineNumber) {
    BlameService blameService = BlameService.getInstance(project);
    return blameService.getDocumentLineBlame(document, file, editorLineNumber);
  }

  @NotNull
  private Collection<LineExtensionInfo> getDecoration(Blame blame) {
    String text = formatBlameText(blame);
    return Collections.singletonList(new LineExtensionInfo(text, blameTextAttributes));
  }

  private String formatBlameText(Blame blame) {
    return new StringBand(2)
        .append(BLAME_PREFIX)
        .append(blame.getShortText())
        .toString();
  }
}

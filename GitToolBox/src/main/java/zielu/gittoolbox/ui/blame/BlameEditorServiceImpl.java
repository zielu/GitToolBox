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
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.FontUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.config.DecorationColors;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.metrics.ProjectMetrics;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.ui.util.AppUiUtil;

class BlameEditorServiceImpl implements BlameEditorService {
  private static final TextAttributesKey ATTRIBUTES_KEY = DecorationColors.EDITOR_INLINE_BLAME_ATTRIBUTES;
  private static final String BLAME_PREFIX = FontUtil.spaceAndThinSpace() + " ";
  private final Logger log = Logger.getInstance(getClass());
  private final AtomicInteger configGeneration = new AtomicInteger(1);
  private final Project project;
  private final BlamePresenter blamePresenter;
  private final VirtualFileRepoCache fileRepoCache;
  private final BlameService blameService;
  private final Timer blameEditorTimer;
  private final Timer blameEditorGetInfoCachingTimer;
  private TextAttributes blameTextAttributes;

  BlameEditorServiceImpl(@NotNull Project project, @NotNull BlamePresenter blamePresenter,
                         @NotNull VirtualFileRepoCache fileRepoCache, @NotNull BlameService blameService,
                         @NotNull ProjectMetrics metrics) {
    this.project = project;
    this.blamePresenter = blamePresenter;
    this.fileRepoCache = fileRepoCache;
    this.blameService = blameService;
    blameEditorTimer = metrics.timer("blame-editor-painter");
    blameEditorGetInfoCachingTimer = metrics.timer("blame-editor-painter-get-info");
    blameTextAttributes = DecorationColors.textAttributes(ATTRIBUTES_KEY);
  }

  @Override
  public void colorsSchemeChanged(@NotNull EditorColorsScheme colorsScheme) {
    blameTextAttributes = colorsScheme.getAttributes(ATTRIBUTES_KEY);
    log.debug("Color scheme updated");
  }

  @Override
  public void configChanged(@NotNull GitToolBoxConfig2 previous, @NotNull GitToolBoxConfig2 current) {
    if (current.isBlameInlinePresentationChanged(previous)) {
      configGeneration.incrementAndGet();
    }
  }

  @Nullable
  @Override
  public Collection<LineExtensionInfo> getLineExtensions(@NotNull VirtualFile file, int editorLineIndex) {
    return blameEditorTimer.timeSupplier(() -> getLineAnnotation(file, editorLineIndex));
  }

  @Nullable
  private Collection<LineExtensionInfo> getLineAnnotation(@NotNull VirtualFile file, int editorLineIndex) {
    if (fileRepoCache.isUnderGitRoot(file)) {
      Document document = FileDocumentManager.getInstance().getDocument(file);
      if (document != null) {
        Editor editor = getEditor(document);
        if (editor != null && isLineWithCaret(editor, editorLineIndex)) {
          return blameEditorGetInfoCachingTimer.timeSupplier(() ->
              getInfosWithCaching(editor, document, file, editorLineIndex));
        }
      }
    }
    return null;
  }

  @Nullable
  private Collection<LineExtensionInfo> getInfos(@NotNull Editor editor, @NotNull Document document,
                                                 @NotNull VirtualFile file, int editorLineIndex) {
    RevisionInfo lineRevisionInfo = getLineBlame(document, file, editorLineIndex);
    Collection<LineExtensionInfo> lineInfo = null;
    if (lineRevisionInfo.isNotEmpty()) {
      lineInfo = getDecoration(lineRevisionInfo);
    }
    return lineInfo;
  }

  @Nullable
  private Collection<LineExtensionInfo> getInfosWithCaching(@NotNull Editor editor, @NotNull Document document,
                                                            @NotNull VirtualFile file, int editorLineIndex) {
    LineState lineState = new LineState(editor, document, editorLineIndex, configGeneration.get());
    Collection<LineExtensionInfo> cachedInfo = lineState.getOrClearCachedLineInfo();
    if (cachedInfo != null) {
      return cachedInfo;
    } else {
      RevisionInfo lineRevisionInfo = getLineBlame(document, file, editorLineIndex);
      Collection<LineExtensionInfo> lineInfo = null;
      if (lineRevisionInfo.isNotEmpty()) {
        lineInfo = getDecoration(lineRevisionInfo);
      }
      lineState.setEditorData(lineInfo);
      return lineInfo;
    }
  }

  @Override
  public void blameUpdated(@NotNull VirtualFile file) {
    AppUiUtil.invokeLaterIfNeeded(project, () -> handleBlameUpdated(file));
  }

  private void handleBlameUpdated(@NotNull VirtualFile file) {
    FileEditor[] editors = FileEditorManager.getInstance(project).getAllEditors(file);
    for (FileEditor editor : editors) {
      BlameEditorData.KEY.set(editor, null);
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

  private boolean isLineWithCaret(@NotNull Editor editor, int editorLineIndex) {
    return BlameUi.getCurrentLineIndex(editor) == editorLineIndex;
  }

  @NotNull
  private RevisionInfo getLineBlame(@NotNull Document document, @NotNull VirtualFile file, int editorLineIndex) {
    return blameService.getDocumentLineIndexBlame(document, file, editorLineIndex);
  }

  @NotNull
  private Collection<LineExtensionInfo> getDecoration(RevisionInfo revisionInfo) {
    String text = formatBlameText(revisionInfo);
    return Collections.singletonList(new LineExtensionInfo(text, blameTextAttributes));
  }

  private String formatBlameText(RevisionInfo revisionInfo) {
    return new StringBand(2)
        .append(BLAME_PREFIX)
        .append(blamePresenter.getEditorInline(revisionInfo))
        .toString();
  }

  private static class LineState {
    private final Editor editor;
    private final int editorLineIndex;
    private final boolean lineModified;
    private final int generation;

    private LineState(@NotNull Editor editor, @NotNull Document document, int editorLineIndex, int generation) {
      this.editor = editor;
      this.editorLineIndex = editorLineIndex;
      this.generation = generation;
      lineModified = document.isLineModified(editorLineIndex);
    }

    void setEditorData(@Nullable Collection<LineExtensionInfo> lineInfo) {
      BlameEditorData editorData = new BlameEditorData(editorLineIndex, lineModified, generation, lineInfo);
      BlameEditorData.KEY.set(editor, editorData);
    }

    @Nullable
    Collection<LineExtensionInfo> getOrClearCachedLineInfo() {
      BlameEditorData editorData = BlameEditorData.KEY.get(editor);
      if (editorData != null) {
        if (editorData.isSameEditorLineIndex(editorLineIndex)
            && editorData.isSameGeneration(generation)
            && editorData.isLineModified() == lineModified) {
          return editorData.getLineInfo();
        } else {
          BlameEditorData.KEY.set(editor, null);
        }
      }
      return null;
    }
  }
}

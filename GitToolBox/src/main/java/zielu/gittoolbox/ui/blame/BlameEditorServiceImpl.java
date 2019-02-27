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
import java.util.concurrent.atomic.AtomicInteger;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.config.DecorationColors;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.metrics.ProjectMetrics;

class BlameEditorServiceImpl implements BlameEditorService {
  private static final TextAttributesKey ATTRIBUTES_KEY = DecorationColors.EDITOR_INLINE_BLAME_ATTRIBUTES;
  private static final String BLAME_PREFIX = FontUtil.spaceAndThinSpace() + " ";
  private final Logger log = Logger.getInstance(getClass());
  private final AtomicInteger configGeneration = new AtomicInteger(1);
  private final Project project;
  private final BlamePresenter blamePresenter;
  private final Timer blameEditorTimer;
  private final Timer blameEditorGetInfoTimer;
  private final Timer blameEditorGetInfoCachingTimer;
  private TextAttributes blameTextAttributes;
  private boolean blameEditorCaching;

  BlameEditorServiceImpl(@NotNull Project project, @NotNull BlamePresenter blamePresenter,
                         @NotNull ProjectMetrics metrics) {
    this.project = project;
    this.blamePresenter = blamePresenter;
    blameEditorTimer = metrics.timer("blame-editor-painter");
    blameEditorGetInfoTimer = metrics.timer("blame-editor-painter-get-info");
    blameEditorGetInfoCachingTimer = metrics.timer("blame-editor-painter-get-info-caching");
    blameTextAttributes = DecorationColors.textAttributes(ATTRIBUTES_KEY);
    blameEditorCaching = GitToolBoxConfig2.getInstance().experimentalBlameEditorCaching;
  }

  @Override
  public void colorsSchemeChanged(@NotNull EditorColorsScheme colorsScheme) {
    blameTextAttributes = colorsScheme.getAttributes(ATTRIBUTES_KEY);
    log.debug("Color scheme updated");
  }

  @Override
  public void configChanged(@NotNull GitToolBoxConfig2 previous, @NotNull GitToolBoxConfig2 current) {
    blameEditorCaching = current.experimentalBlameEditorCaching;
    if (current.isBlameInlinePresentationChanged(previous)) {
      configGeneration.incrementAndGet();
    }
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
          if (blameEditorCaching) {
            return blameEditorGetInfoCachingTimer.timeSupplier(() ->
                getInfosWithCaching(editor, document, file, editorLineNumber));
          } else {
            return blameEditorGetInfoTimer.timeSupplier(() ->
                getInfos(editor, document, file, editorLineNumber));
          }
        }
      }
    }
    return null;
  }

  @Nullable
  private Collection<LineExtensionInfo> getInfos(@NotNull Editor editor, @NotNull Document document,
                                                 @NotNull VirtualFile file, int editorLineNumber) {
    RevisionInfo lineRevisionInfo = getLineBlame(document, file, editorLineNumber);
    Collection<LineExtensionInfo> lineInfo = null;
    if (lineRevisionInfo.isNotEmpty()) {
      lineInfo = getDecoration(lineRevisionInfo);
    }
    return lineInfo;
  }

  @Nullable
  private Collection<LineExtensionInfo> getInfosWithCaching(@NotNull Editor editor, @NotNull Document document,
                                                            @NotNull VirtualFile file, int editorLineNumber) {
    LineState lineState = new LineState(editor, document, editorLineNumber, configGeneration.get());
    Collection<LineExtensionInfo> cachedInfo = lineState.getOrClearCachedLineInfo();
    if (cachedInfo != null) {
      return cachedInfo;
    } else {
      RevisionInfo lineRevisionInfo = getLineBlame(document, file, editorLineNumber);
      Collection<LineExtensionInfo> lineInfo = null;
      if (lineRevisionInfo.isNotEmpty()) {
        lineInfo = getDecoration(lineRevisionInfo);
      }
      lineState.setEditorData(lineInfo);
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

  @NotNull
  private RevisionInfo getLineBlame(@NotNull Document document, @NotNull VirtualFile file, int editorLineNumber) {
    BlameService blameService = BlameService.getInstance(project);
    return blameService.getDocumentLineBlame(document, file, editorLineNumber);
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
    private final int editorLine;
    private final boolean lineModified;
    private final int generation;

    private LineState(@NotNull Editor editor, @NotNull Document document, int editorLine, int generation) {
      this.editor = editor;
      this.editorLine = editorLine;
      this.generation = generation;
      lineModified = document.isLineModified(editorLine);
    }

    void setEditorData(@Nullable Collection<LineExtensionInfo> lineInfo) {
      BlameEditorData editorData = new BlameEditorData(editorLine, lineModified, generation, lineInfo);
      BlameEditorData.KEY.set(editor, editorData);
    }

    @Nullable
    Collection<LineExtensionInfo> getOrClearCachedLineInfo() {
      BlameEditorData editorData = BlameEditorData.KEY.get(editor);
      if (editorData != null) {
        if (editorData.isSameEditorLine(editorLine)
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

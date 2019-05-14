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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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

class BlameUiServiceImpl implements BlameUiService {
  private static final TextAttributesKey ATTRIBUTES_KEY = DecorationColors.EDITOR_INLINE_BLAME_ATTRIBUTES;
  private static final String BLAME_PREFIX = FontUtil.spaceAndThinSpace() + " ";
  private final Logger log = Logger.getInstance(getClass());
  private final AtomicInteger configGeneration = new AtomicInteger(1);
  private final Project project;
  private final BlamePresenter blamePresenter;
  private final VirtualFileRepoCache fileRepoCache;
  private final BlameService blameService;
  private final Timer blameEditorTimer;
  private final Timer blameStatusBarTimer;
  private final Timer blameGetEditorInfoTimer;
  private final Timer blameGetStatusBarInfoTimer;
  private TextAttributes blameTextAttributes;

  BlameUiServiceImpl(@NotNull Project project, @NotNull BlamePresenter blamePresenter,
                     @NotNull VirtualFileRepoCache fileRepoCache, @NotNull BlameService blameService,
                     @NotNull ProjectMetrics metrics) {
    this.project = project;
    this.blamePresenter = blamePresenter;
    this.fileRepoCache = fileRepoCache;
    this.blameService = blameService;
    blameEditorTimer = metrics.timer("blame-editor-painter");
    blameStatusBarTimer = metrics.timer("blame-status-bar");
    blameGetEditorInfoTimer = metrics.timer("blame-editor-get-info");
    blameGetStatusBarInfoTimer = metrics.timer("blame-status-bar-get-info");
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
  public String getBlameStatus(@NotNull VirtualFile file, int editorLineIndex) {
    return blameStatusBarTimer.timeSupplier(() -> getBlameStatusInternal(file, editorLineIndex));
  }

  @Nullable
  private String getBlameStatusInternal(@NotNull VirtualFile file, int editorLineIndex) {
    if (fileRepoCache.isUnderGitRoot(file)) {
      Document document = FileDocumentManager.getInstance().getDocument(file);
      if (isDocumentValid(document)) {
        Editor editor = getEditor(document);
        if (editor != null) {
          return blameGetStatusBarInfoTimer.timeSupplier(() ->
              getStatusWithCaching(editor, document, file, editorLineIndex));
        }
      }
    }
    return null;
  }

  private boolean isDocumentValid(Document document) {
    return document != null;
  }

  @Nullable
  @Override
  public Collection<LineExtensionInfo> getLineExtensions(@NotNull VirtualFile file, int editorLineIndex) {
    return blameEditorTimer.timeSupplier(() -> getLineExtensionsInternal(file, editorLineIndex));
  }

  @Nullable
  private List<LineExtensionInfo> getLineExtensionsInternal(@NotNull VirtualFile file, int editorLineIndex) {
    if (fileRepoCache.isUnderGitRoot(file)) {
      Document document = FileDocumentManager.getInstance().getDocument(file);
      if (isDocumentValid(document)) {
        Editor editor = getEditor(document);
        if (editor != null && isLineWithCaret(editor, editorLineIndex)) {
          return blameGetEditorInfoTimer.timeSupplier(() ->
              getLineInfosWithCaching(editor, document, file, editorLineIndex));
        }
      }
    }
    return null;
  }

  @Nullable
  private List<LineExtensionInfo> getLineInfosWithCaching(@NotNull Editor editor, @NotNull Document document,
                                                          @NotNull VirtualFile file, int editorLineIndex) {
    LineState lineState = new LineState(editor, document, editorLineIndex, configGeneration.get());
    List<LineExtensionInfo> cachedInfo = lineState.getOrClearCachedLineInfo(this::getLineInfoDecoration);
    if (cachedInfo != null) {
      return cachedInfo;
    } else {
      RevisionInfo lineRevisionInfo = getLineBlame(document, file, editorLineIndex);
      if (lineRevisionInfo.isNotEmpty()) {
        lineState.setEditorData(lineRevisionInfo);
      }
      return lineState.getOrClearCachedLineInfo(this::getLineInfoDecoration);
    }
  }

  @Nullable
  private String getStatusWithCaching(@NotNull Editor editor, @NotNull Document document, @NotNull VirtualFile file,
                                      int editorLineIndex) {
    LineState lineState = new LineState(editor, document, editorLineIndex, configGeneration.get());
    String cachedInfo = lineState.getOrClearCachedStatus(this::getStatusDecoration);
    if (cachedInfo != null) {
      return cachedInfo;
    } else {
      RevisionInfo lineRevisionInfo = getLineBlame(document, file, editorLineIndex);
      if (lineRevisionInfo.isNotEmpty()) {
        lineState.setEditorData(lineRevisionInfo);
      }
      return lineState.getOrClearCachedStatus(this::getStatusDecoration);
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
      BlameEditorLineData.KEY.set(editor, null);
      BlameStatusLineData.KEY.set(editor, null);
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
  private List<LineExtensionInfo> getLineInfoDecoration(RevisionInfo revisionInfo) {
    String text = formatBlameText(revisionInfo);
    return Collections.singletonList(new LineExtensionInfo(text, blameTextAttributes));
  }

  private String getStatusDecoration(@NotNull RevisionInfo revisionInfo) {
    return blamePresenter.getStatusBar(revisionInfo);
  }

  private String formatBlameText(@NotNull RevisionInfo revisionInfo) {
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

    void setEditorData(@NotNull RevisionInfo revisionInfo) {
      BlameEditorData editorData = new BlameEditorData(editorLineIndex, lineModified, generation, revisionInfo);
      BlameEditorData.KEY.set(editor, editorData);
    }

    @Nullable
    List<LineExtensionInfo> getOrClearCachedLineInfo(Function<RevisionInfo, List<LineExtensionInfo>> lineInfoMaker) {
      BlameEditorData editorData = BlameEditorData.KEY.get(editor);
      if (editorData != null) {
        if (isSameEditorData(editorData)) {
          RevisionInfo revisionInfo = editorData.getRevisionInfo();
          BlameEditorLineData lineData = BlameEditorLineData.KEY.get(editor);
          if (lineData != null && lineData.isSameRevision(revisionInfo)) {
            return lineData.getLineInfo();
          } else {
            List<LineExtensionInfo> lineInfo = lineInfoMaker.apply(revisionInfo);
            BlameEditorLineData.KEY.set(editor, new BlameEditorLineData(lineInfo, revisionInfo));
            return lineInfo;
          }
        } else {
          BlameEditorData.KEY.set(editor, null);
          BlameEditorLineData.KEY.set(editor, null);
          BlameStatusLineData.KEY.set(editor, null);
        }
      }
      return null;
    }

    private boolean isSameEditorData(BlameEditorData editorData) {
      return editorData.isSameEditorLineIndex(editorLineIndex)
          && editorData.isSameGeneration(generation)
          && editorData.isLineModified() == lineModified;
    }

    @Nullable
    String getOrClearCachedStatus(Function<RevisionInfo, String> statusMaker) {
      BlameEditorData editorData = BlameEditorData.KEY.get(editor);
      if (editorData != null) {
        if (isSameEditorData(editorData)) {
          RevisionInfo revisionInfo = editorData.getRevisionInfo();
          BlameStatusLineData lineData = BlameStatusLineData.KEY.get(editor);
          if (lineData != null && lineData.isSameRevision(revisionInfo)) {
            return lineData.getLineInfo();
          } else {
            String lineInfo = statusMaker.apply(revisionInfo);
            BlameStatusLineData.KEY.set(editor, new BlameStatusLineData(lineInfo, revisionInfo));
            return lineInfo;
          }
        } else {
          BlameEditorData.KEY.set(editor, null);
          BlameEditorLineData.KEY.set(editor, null);
          BlameStatusLineData.KEY.set(editor, null);
        }
      }
      return null;
    }
  }
}

package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.FontUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.config.DecorationColors;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.ui.util.AppUiUtil;
import zielu.intellij.util.ZDisposeGuard;
import zielu.intellij.util.ZUtil;

class BlameUiServiceImpl implements BlameUiService, Disposable {
  private static final TextAttributesKey ATTRIBUTES_KEY = DecorationColors.EDITOR_INLINE_BLAME_ATTRIBUTES;
  private static final String BLAME_PREFIX = FontUtil.spaceAndThinSpace() + " ";
  private final Logger log = Logger.getInstance(getClass());
  private final ZDisposeGuard disposeGuard = new ZDisposeGuard();
  private final AtomicInteger configGeneration = new AtomicInteger(1);
  private final Project project;
  private final BlameUiServiceFacade facade;
  private TextAttributes blameTextAttributes;

  BlameUiServiceImpl(@NotNull Project project) {
    this.project = project;
    this.facade = new BlameUiServiceFacade(project, ATTRIBUTES_KEY);
    this.facade.registerDisposable(this, disposeGuard);
  }

  @Override
  public void colorsSchemeChanged(@NotNull EditorColorsScheme colorsScheme) {
    blameTextAttributes = colorsScheme.getAttributes(ATTRIBUTES_KEY);
    log.debug("Color scheme updated");
  }

  @Override
  public void configChanged(@NotNull GitToolBoxConfig2 previous, @NotNull GitToolBoxConfig2 current) {
    if (current.isBlameInlinePresentationChanged(previous)
        || current.isBlameStatusPresentationChanged(previous)) {
      configGeneration.incrementAndGet();
    }
  }

  @Override
  public void refreshBlame() {
    if (disposeGuard.isActive()) {
      facade.invalidateAllBlames();
    }
  }

  @Nullable
  @Override
  public String getBlameStatus(@NotNull VirtualFile file, int editorLineIndex) {
    if (disposeGuard.isActive()) {
      return facade.getStatusBarTimer().timeSupplier(() -> getBlameStatusInternal(file, editorLineIndex));
    }
    return null;
  }

  @Nullable
  private String getBlameStatusInternal(@NotNull VirtualFile file, int editorLineIndex) {
    LineInfo lineInfo = createLineInfo(file, editorLineIndex);
    if (lineInfo != null) {
      return facade.getStatusBarInfoTimer().timeSupplier(() -> getStatusWithCaching(lineInfo));
    }
    return null;
  }

  @Nullable
  private LineInfo createLineInfo(@NotNull VirtualFile file, int lineIndex) {
    if (facade.isUnderGit(file)) {
      Document document = facade.getDocument(file);
      if (isDocumentValid(document, lineIndex)) {
        Editor editor = facade.getEditorIfDocumentSelected(document);
        if (editor != null) {
          return new LineInfo(file, editor, document, lineIndex, configGeneration.get());
        }
      }
    }
    return null;
  }

  private boolean isDocumentValid(Document document, int lineIndex) {
    return document != null && lineIndex < document.getLineCount();
  }

  @Nullable
  @Override
  public String getBlameStatusTooltip(@NotNull VirtualFile file, int editorLineIndex) {
    if (disposeGuard.isActive() && facade.isUnderGit(file)) {
      Document document = facade.getDocument(file);
      if (isDocumentValid(document, editorLineIndex)) {
        RevisionInfo revisionInfo = facade.getLineBlame(document, file, editorLineIndex);
        return facade.getBlameStatusTooltip(revisionInfo);
      }
    }
    return null;
  }

  @Nullable
  @Override
  public Collection<LineExtensionInfo> getLineExtensions(@NotNull VirtualFile file, int editorLineIndex) {
    if (disposeGuard.isActive()) {
      return facade.getEditorTimer().timeSupplier(() -> getLineExtensionsInternal(file, editorLineIndex));
    } else {
      return null;
    }
  }

  @Nullable
  private List<LineExtensionInfo> getLineExtensionsInternal(@NotNull VirtualFile file, int editorLineIndex) {
    LineInfo lineInfo = createLineInfo(file, editorLineIndex);
    if (lineInfo != null && isLineWithCaret(lineInfo.getEditor(), lineInfo.getIndex())) {
      return facade.getEditorInfoTimer().timeSupplier(() -> getLineInfosWithCaching(lineInfo));
    }
    return null;
  }

  @Nullable
  private List<LineExtensionInfo> getLineInfosWithCaching(@NotNull LineInfo lineInfo) {
    LineState lineState = new LineState(lineInfo);
    List<LineExtensionInfo> cachedInfo = lineState.getOrClearCachedLineInfo(this::getLineInfoDecoration);
    if (cachedInfo != null) {
      return cachedInfo;
    } else {
      RevisionInfo lineRevisionInfo = facade.getLineBlame(lineInfo);
      if (lineRevisionInfo.isNotEmpty()) {
        lineState.setRevisionInfo(lineRevisionInfo);
      }
      return lineState.getOrClearCachedLineInfo(this::getLineInfoDecoration);
    }
  }

  @Nullable
  private String getStatusWithCaching(@NotNull LineInfo lineInfo) {
    LineState lineState = new LineState(lineInfo);
    String cachedInfo = lineState.getOrClearCachedStatus(facade::getStatusDecoration);
    if (cachedInfo != null) {
      return cachedInfo;
    } else {
      RevisionInfo lineRevisionInfo = facade.getLineBlame(lineInfo);
      if (lineRevisionInfo.isNotEmpty()) {
        lineState.setRevisionInfo(lineRevisionInfo);
      }
      return lineState.getOrClearCachedStatus(facade::getStatusDecoration);
    }
  }

  @Override
  public void blameUpdated(@NotNull VirtualFile file) {
    if (disposeGuard.isActive()) {
      log.debug("Blame updated: ", file);
      AppUiUtil.invokeLaterIfNeeded(disposeGuard, () -> handleBlameUpdated(file));
    }
  }

  private void handleBlameUpdated(@NotNull VirtualFile file) {
    facade.getAllEditors(file).forEach(editor -> {
      LineState.clear(editor);
      log.debug("Cleared line state: ", file, ", editor=", editor);
    });
  }

  private boolean isLineWithCaret(@NotNull Editor editor, int editorLineIndex) {
    return facade.getCurrentLineIndex(editor) == editorLineIndex;
  }

  @NotNull
  private List<LineExtensionInfo> getLineInfoDecoration(RevisionInfo revisionInfo) {
    String text = formatBlameText(revisionInfo);
    return Collections.singletonList(new LineExtensionInfo(text, getBlameTextAttributes()));
  }

  private TextAttributes getBlameTextAttributes() {
    return ZUtil.defaultIfNull(blameTextAttributes, facade::getDefaultBlameTextAttributes);
  }

  private String formatBlameText(@NotNull RevisionInfo revisionInfo) {
    return new StringBand(2)
        .append(BLAME_PREFIX)
        .append(facade.getEditorInlineDecoration(revisionInfo))
        .toString();
  }

  @Override
  public void dispose() {
    clearData();
  }

  private void clearData() {
    Arrays.stream(FileEditorManager.getInstance(project).getAllEditors())
        .filter(TextEditor.class::isInstance)
        .map(TextEditor.class::cast)
        .map(TextEditor::getEditor)
        .forEach(this::clearEditorData);
  }

  private void clearEditorData(Editor editor) {
    BlameEditorData.clear(editor);
    BlameEditorLineData.clear(editor);
    BlameStatusLineData.clear(editor);
  }
}

package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.fileEditor.FileEditor;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionInfo;

class LineState {
  private final LineInfo lineInfo;

  LineState(@NotNull LineInfo lineInfo) {
    this.lineInfo = lineInfo;
  }

  void setRevisionInfo(@NotNull RevisionInfo revisionInfo) {
    BlameEditorData editorData = new BlameEditorData(lineInfo.lineIndex, lineInfo.lineModified, lineInfo.generation,
        revisionInfo);
    BlameEditorData.KEY.set(lineInfo.editor, editorData);
  }

  @Nullable
  List<LineExtensionInfo> getOrClearCachedLineInfo(Function<RevisionInfo, List<LineExtensionInfo>> lineInfoMaker) {
    BlameEditorData editorData = BlameEditorData.KEY.get(lineInfo.editor);
    if (editorData != null) {
      if (isSameEditorData(editorData)) {
        RevisionInfo revisionInfo = editorData.getRevisionInfo();
        BlameEditorLineData lineData = BlameEditorLineData.KEY.get(lineInfo.editor);
        if (lineData != null && lineData.isSameRevision(revisionInfo)) {
          return lineData.getLineInfo();
        } else {
          List<LineExtensionInfo> lineExtensions = lineInfoMaker.apply(revisionInfo);
          BlameEditorLineData.KEY.set(lineInfo.editor, new BlameEditorLineData(lineExtensions, revisionInfo));
          return lineExtensions;
        }
      } else {
        clear();
      }
    }
    return null;
  }

  private void clear() {
    BlameEditorData.KEY.set(lineInfo.editor, null);
    BlameEditorLineData.KEY.set(lineInfo.editor, null);
    BlameStatusLineData.KEY.set(lineInfo.editor, null);
  }

  static void clear(@NotNull FileEditor editor) {
    BlameEditorData.KEY.set(editor, null);
    BlameEditorLineData.KEY.set(editor, null);
    BlameStatusLineData.KEY.set(editor, null);
  }

  private boolean isSameEditorData(BlameEditorData editorData) {
    return editorData.isSameEditorLineIndex(lineInfo.lineIndex)
        && editorData.isSameGeneration(lineInfo.generation)
        && editorData.isLineModified() == lineInfo.lineModified;
  }

  @Nullable
  String getOrClearCachedStatus(Function<RevisionInfo, String> statusMaker) {
    BlameEditorData editorData = BlameEditorData.KEY.get(lineInfo.editor);
    if (editorData != null) {
      if (isSameEditorData(editorData)) {
        RevisionInfo revisionInfo = editorData.getRevisionInfo();
        BlameStatusLineData lineData = BlameStatusLineData.KEY.get(lineInfo.editor);
        if (lineData != null && lineData.isSameRevision(revisionInfo)) {
          return lineData.getLineInfo();
        } else {
          String statusText = statusMaker.apply(revisionInfo);
          BlameStatusLineData.KEY.set(lineInfo.editor, new BlameStatusLineData(statusText, revisionInfo));
          return statusText;
        }
      } else {
        clear();
      }
    }
    return null;
  }
}

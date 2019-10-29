package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LineExtensionInfo;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionInfo;

class LineState {
  private static final Logger LOG = Logger.getInstance(LineState.class);
  private final LineInfo lineInfo;

  LineState(@NotNull LineInfo lineInfo) {
    this.lineInfo = lineInfo;
  }

  void setRevisionInfo(@NotNull RevisionInfo revisionInfo) {
    BlameEditorData editorData = new BlameEditorData(lineInfo.getIndex(), lineInfo.getModified(),
        lineInfo.getGeneration(), revisionInfo);
    BlameEditorData.set(lineInfo.getEditor(), editorData);
  }

  @Nullable
  List<LineExtensionInfo> getOrClearCachedLineInfo(Function<RevisionInfo, List<LineExtensionInfo>> lineInfoMaker) {
    BlameEditorData editorData = BlameEditorData.get(lineInfo.getEditor());
    final boolean trace = LOG.isTraceEnabled();
    if (editorData != null) {
      if (isSameEditorData(editorData)) {
        if (trace) {
          LOG.trace("BlameEditorData is same: " + editorData);
        }
        RevisionInfo revisionInfo = editorData.getRevisionInfo();
        BlameEditorLineData lineData = BlameEditorLineData.get(lineInfo.getEditor());
        if (lineData != null && lineData.isSameRevision(revisionInfo)) {
          if (trace) {
            LOG.trace("Same revision: " + revisionInfo + " == " + lineData);
          }
          return lineData.getLineInfo();
        } else {
          List<LineExtensionInfo> lineExtensions = lineInfoMaker.apply(revisionInfo);
          BlameEditorLineData newLineData = new BlameEditorLineData(revisionInfo, lineExtensions);
          BlameEditorLineData.set(lineInfo.getEditor(), newLineData);
          if (trace) {
            LOG.trace("New BlameEditorLineData: " + newLineData);
          }
          return lineExtensions;
        }
      } else {
        clear();
        if (trace) {
          LOG.trace("BlameEditorData cleared: " + editorData);
        }
      }
    } else {
      if (trace) {
        LOG.trace("BlameEditorData: null");
      }
    }
    return null;
  }

  private void clear() {
    clear(lineInfo.getEditor());
  }

  static void clear(@NotNull Editor editor) {
    BlameEditorData.clear(editor);
    BlameEditorLineData.clear(editor);
    BlameStatusLineData.clear(editor);
  }

  private boolean isSameEditorData(BlameEditorData editorData) {
    return editorData.isSameEditorLineIndex(lineInfo.getIndex())
        && editorData.isSameGeneration(lineInfo.getGeneration())
        && editorData.getLineModified() == lineInfo.getModified();
  }

  @Nullable
  String getOrClearCachedStatus(Function<RevisionInfo, String> statusMaker) {
    BlameEditorData editorData = BlameEditorData.get(lineInfo.getEditor());
    if (editorData != null) {
      if (isSameEditorData(editorData)) {
        RevisionInfo revisionInfo = editorData.getRevisionInfo();
        BlameStatusLineData lineData = BlameStatusLineData.get(lineInfo.getEditor());
        if (lineData != null && lineData.isSameRevision(revisionInfo)) {
          return lineData.getLineInfo();
        } else {
          String statusText = statusMaker.apply(revisionInfo);
          BlameStatusLineData.set(lineInfo.getEditor(), new BlameStatusLineData(revisionInfo, statusText));
          return statusText;
        }
      } else {
        clear();
      }
    }
    return null;
  }
}

package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionInfo;

class BlameStateHolder {
  // store editor here to avoid expensive and EDT-only getSelectedEditor() retrievals
  private volatile Reference<Editor> editor = new WeakReference<>(null);
  private volatile Reference<VirtualFile> file = new WeakReference<>(null);
  private volatile Reference<RevisionInfo> blame = new WeakReference<>(RevisionInfo.EMPTY);

  boolean isCurrentEditorDocument(@Nullable Document document) {
    Editor selectedEditor = editor.get();
    return document != null && (selectedEditor == null || selectedEditor.getDocument() != document);
  }

  @Nullable
  Editor updateCurrentEditor(@Nullable Editor editor) {
    this.editor = new WeakReference<>(editor);
    return editor;
  }

  @Nullable
  Editor getCurrentEditor() {
    return editor.get();
  }

  @Nullable
  VirtualFile getCurrentFile() {
    return file.get();
  }

  void updateCurrentFile(@Nullable Document document) {
    if (document != null) {
      file = new WeakReference<>(FileDocumentManager.getInstance().getFile(document));
    } else {
      file = new WeakReference<>(null);
    }
  }

  void updateCurrentFile(@Nullable VirtualFile file) {
    this.file = new WeakReference<>(file);
  }

  boolean updateBlame(@NotNull RevisionInfo revisionInfo) {
    RevisionInfo currentRevisionInfo = this.blame.get();
    if (!Objects.equals(revisionInfo, currentRevisionInfo)) {
      this.blame = new WeakReference<>(revisionInfo);
      return true;
    }
    return false;
  }

  @NotNull
  RevisionInfo getBlame() {
    RevisionInfo revisionInfo = this.blame.get();
    return revisionInfo != null ? revisionInfo : RevisionInfo.EMPTY;
  }

  boolean clearBlame() {
    if (blame.get() != null) {
      blame = new WeakReference<>(null);
      return true;
    }
    return false;
  }
}

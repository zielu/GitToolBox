package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.blame.Blame;

class BlameStateHolder {
  // store editor here to avoid expensive and EDT-only getSelectedEditor() retrievals
  private volatile Reference<Editor> editor = new WeakReference<>(null);
  private volatile Reference<VirtualFile> file = new WeakReference<>(null);
  private volatile Reference<Blame> blame = new WeakReference<>(null);

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

  boolean updateBlame(@Nullable Blame blame) {
    Blame currentBlame = this.blame.get();
    if (!Objects.equals(blame, currentBlame)) {
      this.blame = new WeakReference<>(blame);
      return true;
    }
    return false;
  }

  @Nullable
  Blame getBlame() {
    return this.blame.get();
  }

  boolean clearBlame() {
    if (blame.get() != null) {
      blame = new WeakReference<>(null);
      return true;
    }
    return false;
  }
}

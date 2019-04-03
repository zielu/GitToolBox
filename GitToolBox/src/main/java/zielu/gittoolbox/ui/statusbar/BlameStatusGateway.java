package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionInfo;

public interface BlameStatusGateway {
  void addDumbModeExitAction(Runnable action);

  void removeDumbModeExitAction(Runnable action);

  void addBulkUpdateFinishedAction(Consumer<Document> action);

  void removeBulkUpdateFinishedAction(Consumer<Document> action);

  void addBlameUpdatedAction(Consumer<VirtualFile> action);

  void removeBlameUpdateAction(Consumer<VirtualFile> action);

  boolean isInBulkUpdate(@Nullable Document document);

  boolean isUnderVcs(@NotNull VirtualFile file);

  @Nullable
  String getCommitMessage(@NotNull RevisionInfo revisionInfo);

  static BlameStatusGateway getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, BlameStatusGateway.class);
  }
}

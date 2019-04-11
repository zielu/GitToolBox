package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.DocumentBulkUpdateListener;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBusConnection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.blame.BlameListener;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.revision.RevisionService;

class BlameStatusGatewayImpl implements BlameStatusGateway, Disposable {
  private final Set<Document> inBulkUpdate = ContainerUtil.newConcurrentSet();
  private final Set<Runnable> exitDumbModeActions = new LinkedHashSet<>();
  private final Set<Consumer<Document>> bulkUpdateFinishedActions = new LinkedHashSet<>();
  private final Set<Consumer<VirtualFile>> blameUpdatedActions = new LinkedHashSet<>();
  private final Project project;
  private final VirtualFileRepoCache repoCache;
  private final MessageBusConnection connection;

  BlameStatusGatewayImpl(@NotNull Project project, @NotNull VirtualFileRepoCache repoCache) {
    this.project = project;
    this.repoCache = repoCache;
    connection = project.getMessageBus().connect(this);
    connection.subscribe(DumbService.DUMB_MODE, new DumbService.DumbModeListener() {
      @Override
      public void exitDumbMode() {
        exitDumbModeActions.forEach(Runnable::run);
      }
    });
    connection.subscribe(DocumentBulkUpdateListener.TOPIC, new DocumentBulkUpdateListener() {
      @Override
      public void updateStarted(@NotNull Document doc) {
        inBulkUpdate.add(doc);
      }

      @Override
      public void updateFinished(@NotNull Document doc) {
        if (inBulkUpdate.remove(doc)) {
          bulkUpdateFinishedActions.forEach(action -> action.consume(doc));
        }
      }
    });
    connection.subscribe(BlameService.BLAME_UPDATE, new BlameListener() {
      @Override
      public void blameUpdated(@NotNull VirtualFile file) {
        notifyBlameActions(file);
      }

      @Override
      public void blameInvalidated(@NotNull VirtualFile file) {
        notifyBlameActions(file);
      }
    });
    Disposer.register(project, this);
  }

  private void notifyBlameActions(@NotNull VirtualFile file) {
    blameUpdatedActions.forEach(action -> action.consume(file));
  }

  @Override
  public void addDumbModeExitAction(Runnable action) {
    exitDumbModeActions.add(action);
  }

  @Override
  public void removeDumbModeExitAction(Runnable action) {
    exitDumbModeActions.remove(action);
  }

  @Override
  public void addBulkUpdateFinishedAction(Consumer<Document> action) {
    bulkUpdateFinishedActions.add(action);
  }

  @Override
  public void removeBulkUpdateFinishedAction(Consumer<Document> action) {
    bulkUpdateFinishedActions.remove(action);
  }

  @Override
  public void addBlameUpdatedAction(Consumer<VirtualFile> action) {
    blameUpdatedActions.add(action);
  }

  @Override
  public void removeBlameUpdateAction(Consumer<VirtualFile> action) {
    blameUpdatedActions.remove(action);
  }

  @Override
  public boolean isInBulkUpdate(@Nullable Document document) {
    return inBulkUpdate.contains(document);
  }

  @Override
  public boolean isUnderVcs(@NotNull VirtualFile file) {
    return repoCache.isUnderGitRoot(file);
  }

  @Override
  public String getCommitMessage(@NotNull RevisionInfo revisionInfo) {
    return RevisionService.getInstance(project).getCommitMessage(revisionInfo);
  }

  @Override
  public void dispose() {
    inBulkUpdate.clear();
    exitDumbModeActions.clear();
    bulkUpdateFinishedActions.clear();
  }
}

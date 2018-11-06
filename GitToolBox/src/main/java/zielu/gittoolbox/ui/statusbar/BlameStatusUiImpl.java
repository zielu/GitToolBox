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
import git4idea.GitVcs;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.GtUtil;

class BlameStatusUiImpl implements BlameStatusUi, Disposable {
  private final Set<Document> inBulkUpdate = ContainerUtil.newConcurrentSet();
  private final Set<Runnable> exitDumbModeActions = new LinkedHashSet<>();
  private final Set<Consumer<Document>> bulkUpdateFinishedActions = new LinkedHashSet<>();
  private final GitVcs git;
  private MessageBusConnection connection;

  BlameStatusUiImpl(@NotNull Project project) {
    git = GitVcs.getInstance(project);
    connection = project.getMessageBus().connect(this);
    Disposer.register(project, this);
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
  public boolean isInBulkUpdate(@Nullable Document document) {
    return inBulkUpdate.contains(document);
  }

  @Override
  public boolean isUnderVcs(@NotNull VirtualFile file) {
    return git.fileIsUnderVcs(GtUtil.localFilePath(file));
  }

  @Override
  public void dispose() {
    connection.disconnect();
    connection = null;
    inBulkUpdate.clear();
    exitDumbModeActions.clear();
    bulkUpdateFinishedActions.clear();
  }
}

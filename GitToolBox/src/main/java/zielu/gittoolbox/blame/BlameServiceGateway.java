package zielu.gittoolbox.blame;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.impl.UpToDateLineNumberProviderImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import git4idea.GitVcs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.GtUtil;

class BlameServiceGateway {
  private final Project project;
  private final GitVcs git;
  private final MessageBus messageBus;

  BlameServiceGateway(@NotNull Project project) {
    this.project = project;
    git = GitVcs.getInstance(project);
    messageBus = project.getMessageBus();
  }

  @NotNull
  UpToDateLineNumberProvider createUpToDateLineProvider(@NotNull Document document) {
    return new UpToDateLineNumberProviderImpl(document, project);
  }

  @Nullable
  VcsFileRevision getLastRevision(@NotNull VirtualFile file) throws VcsException {
    return git.getVcsHistoryProvider().getLastRevision(GtUtil.localFilePath(file));
  }

  void fireBlameUpdated(@NotNull VirtualFile file) {
    messageBus.syncPublisher(BlameService.BLAME_UPDATE).blameUpdated(file);
  }

  void fireBlameInvalidated(@NotNull VirtualFile file) {
    messageBus.syncPublisher(BlameService.BLAME_UPDATE).blameInvalidated(file);
  }
}

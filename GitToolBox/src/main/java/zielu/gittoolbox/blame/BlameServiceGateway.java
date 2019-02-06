package zielu.gittoolbox.blame;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.impl.UpToDateLineNumberProviderImpl;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.GtUtil;

class BlameServiceGateway {
  private final Project project;

  BlameServiceGateway(Project project) {
    this.project = project;
  }

  UpToDateLineNumberProvider createUpToDateLineProvider(@NotNull Document document) {
    return new UpToDateLineNumberProviderImpl(document, project);
  }

  @Nullable
  VcsFileRevision getLastRevision(@NotNull VirtualFile file) throws VcsException {
    return getGit().getVcsHistoryProvider().getLastRevision(GtUtil.localFilePath(file));
  }

  private GitVcs getGit() {
    return GitVcs.getInstance(project);
  }

  void fireBlameUpdated(@NotNull VirtualFile file) {
    project.getMessageBus().syncPublisher(BlameService.BLAME_UPDATE).blameUpdated(file);
  }

  void fireBlameInvalidated(@NotNull VirtualFile file) {
    project.getMessageBus().syncPublisher(BlameService.BLAME_UPDATE).blameInvalidated(file);
  }
}

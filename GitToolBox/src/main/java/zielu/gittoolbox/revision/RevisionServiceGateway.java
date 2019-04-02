package zielu.gittoolbox.revision;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcs.log.data.VcsLogData;
import com.intellij.vcs.log.data.index.IndexDataGetter;
import com.intellij.vcs.log.impl.HashImpl;
import com.intellij.vcs.log.impl.VcsLogManager;
import com.intellij.vcs.log.impl.VcsProjectLog;
import git4idea.repo.GitRepository;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.util.GatewayBase;

class RevisionServiceGateway extends GatewayBase {
  RevisionServiceGateway(@NotNull Project project) {
    super(project);
  }

  void fireRevisionUpdated(RevisionInfo revisionInfo) {
    project.getMessageBus().syncPublisher(RevisionService.UPDATES).revisionUpdated(revisionInfo);
  }

  @Nullable
  VirtualFile rootForFile(@NotNull VirtualFile file) {
    return Optional.ofNullable(VirtualFileRepoCache.getInstance(project).getRepoForFile(file))
        .map(GitRepository::getRoot).orElse(null);
  }

  @Nullable
  String loadDetails(@NotNull VcsRevisionNumber revisionNumber, @NotNull VirtualFile root) {
    VcsLogManager logManager = VcsProjectLog.getInstance(project).getLogManager();
    if (logManager != null) {
      VcsLogData dataManager = logManager.getDataManager();
      IndexDataGetter getter = dataManager.getIndex().getDataGetter();
      if (getter != null) {
        int commitIndex = dataManager.getCommitIndex(HashImpl.build(revisionNumber.asString()), root);
        return getter.getFullMessage(commitIndex);
      }
    }
    return null;
  }
}

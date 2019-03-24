package zielu.gittoolbox.revision;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcs.log.VcsCommitMetadata;
import git4idea.history.GitHistoryUtils;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.cache.VirtualFileRepoCache;

class DefaultRevisionInfoFactory implements RevisionInfoFactory {
  private final Project project;
  private final VirtualFileRepoCache repoCache;

  DefaultRevisionInfoFactory(@NotNull Project project, @NotNull VirtualFileRepoCache repoCache) {
    this.project = project;
    this.repoCache = repoCache;
  }

  @NotNull
  @Override
  public RevisionInfo forLine(@NotNull FileAnnotation annotation, @NotNull VcsRevisionNumber lineRevision,
                              int lineNumber) {
    return fromFileRevision(annotation, lineRevision);
  }

  @NotNull
  @Override
  public RevisionInfo forFile(@NotNull VirtualFile file, @NotNull VcsFileRevision revision) {
    VcsRevisionNumber revisionNumber = revision.getRevisionNumber();
    return infoFor(revisionNumber, getMetadata(project, file, revisionNumber));
  }

  private RevisionInfo fromFileRevision(FileAnnotation annotation, VcsRevisionNumber revision) {
    VcsCommitMetadata metadata = getMetadata(annotation, revision);
    return infoFor(revision, metadata);
  }

  private RevisionInfo infoFor(VcsRevisionNumber revision, @Nullable VcsCommitMetadata metadata) {
    if (metadata != null) {
      return new RevisionInfoImpl(revision, metadata.getAuthor().getName(),
          Date.from(Instant.ofEpochMilli(metadata.getAuthorTime())), metadata.getSubject(),
          metadata.getFullMessage());
    }
    return RevisionInfo.EMPTY;
  }

  private VcsCommitMetadata getMetadata(FileAnnotation annotation, VcsRevisionNumber revision) {
    Project project = annotation.getProject();
    VirtualFile file = annotation.getFile();
    return getMetadata(project, file, revision);
  }

  private VcsCommitMetadata getMetadata(Project project, VirtualFile file, VcsRevisionNumber revision) {
    VirtualFile root = repoCache.getRepoRootForFile(file);
    if (root != null) {
      try {
        List<? extends VcsCommitMetadata> metadata = GitHistoryUtils.collectCommitsMetadata(project, root,
            revision.asString());
        if (metadata != null) {
          return metadata.get(0);
        }
      } catch (VcsException e) {
        return null;
      }
    }
    return null;
  }
}

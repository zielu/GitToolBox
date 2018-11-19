package zielu.gittoolbox.blame;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.cache.VirtualFileRepoCache;

class BlameCacheImpl implements BlameCache {
  private static final BlameAnnotation EMPTY = new BlameAnnotation() {
    @Nullable
    @Override
    public Blame getBlame(int lineNumber) {
      return null;
    }

    @Override
    public boolean isChanged(@NotNull VcsRevisionNumber revision) {
      return !VcsRevisionNumber.NULL.equals(revision);
    }

    @Nullable
    @Override
    public VirtualFile getVirtualFile() {
      return null;
    }
  };
  private static final Logger log = Logger.getInstance(BlameCacheImpl.class);
  private final Project project;
  private final VirtualFileRepoCache fileRepoCache;
  private final Map<VirtualFile, BlameAnnotation> annotations = new ConcurrentHashMap<>();
  private final Set<VirtualFile> queued = ContainerUtil.newConcurrentSet();
  private final GitVcs git;

  private ExecutorService executor;

  BlameCacheImpl(@NotNull Project project, @NotNull VirtualFileRepoCache fileRepoCache) {
    this.project = project;
    this.fileRepoCache = fileRepoCache;
    git = GitVcs.getInstance(project);
    executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
        .setDaemon(true)
        .setNameFormat("blame-cache-%d")
        .build()
    );
  }

  @NotNull
  @Override
  public BlameAnnotation getAnnotation(@NotNull VirtualFile file) {
    return annotations.compute(file, (fileKey, existingAnnotation) -> {
      if (existingAnnotation == null) {
        submitTask(fileKey);
        return EMPTY;
      } else {
        return handleExistingAnnotation(fileKey, existingAnnotation);
      }
    });
  }

  private void submitTask(@NotNull VirtualFile file) {
    if (queued.add(file)) {
      executor.execute(new FileAnnotationLoader(file, git, this::updateAnnotation));
    }
  }

  @NotNull
  private BlameAnnotation handleExistingAnnotation(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    VcsRevisionNumber currentRevision = currentRepoRevision(file);
    if (annotation.isChanged(currentRevision)) {
      submitTask(file);
      return EMPTY;
    } else {
      return annotation;
    }
  }

  private void updateAnnotation(@NotNull FileAnnotation fileAnnotation) {
    VirtualFile file = fileAnnotation.getFile();
    if (file != null && queued.remove(file)) {
      BlameAnnotation blameAnnotation = new BlameAnnotationImpl(fileAnnotation);
      annotations.put(file, blameAnnotation);
      fireUpdated(file, blameAnnotation);
    }
  }

  private void fireUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    project.getMessageBus().syncPublisher(BlameCache.TOPIC).cacheUpdated(file, annotation);
  }

  @NotNull
  private VcsRevisionNumber currentRepoRevision(@NotNull VirtualFile file) {
    GitRepository repo = fileRepoCache.getRepoForFile(file);
    if (repo != null) {
      try {
        VcsRevisionNumber parsedRevision = git.parseRevisionNumber(repo.getCurrentRevision());
        if (parsedRevision != null) {
          return parsedRevision;
        }
      } catch (VcsException e) {
        log.warn("Could not get current repoRevision for " + file);
      }
    }
    return VcsRevisionNumber.NULL;
  }

  @Override
  public void invalidate(@NotNull VirtualFile file) {
    annotations.remove(file);
  }

  private static class FileAnnotationLoader implements Runnable {
    private final VirtualFile file;
    private final GitVcs git;
    private final Consumer<FileAnnotation> loaded;

    private FileAnnotationLoader(@NotNull VirtualFile file, @NotNull GitVcs git, @NotNull Consumer<FileAnnotation> loaded) {
      this.file = file;
      this.git = git;
      this.loaded = loaded;
    }

    @Override
    public void run() {
      try {
        FileAnnotation fileAnnotation = git.getAnnotationProvider().annotate(file);
        loaded.accept(fileAnnotation);
      } catch (VcsException e) {
        log.warn("Failed to annotate " + file, e);
      }
    }
  }
}

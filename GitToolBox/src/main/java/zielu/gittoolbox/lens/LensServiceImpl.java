package zielu.gittoolbox.lens;

import com.codahale.metrics.Timer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.LocalFilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.impl.UpToDateLineNumberProviderImpl;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.MetricsHost;

class LensServiceImpl implements LensService {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;
  private final Timer fileBlameTimer;
  private final Timer lineBlameTimer;

  LensServiceImpl(@NotNull Project project) {
    this.project = project;
    Metrics metrics = MetricsHost.project(project);
    fileBlameTimer = metrics.timer("lens-file-blame");
    lineBlameTimer = metrics.timer("lens-line-blame");
  }

  @Nullable
  @Override
  public LensBlame getFileBlame(@NotNull VirtualFile file) {
    return fileBlameTimer.timeSupplier(() -> getFileBlameInternal(file));
  }

  @Nullable
  private LensBlame getFileBlameInternal(@NotNull VirtualFile file) {
    GitVcs git = getGit();
    LensBlame blame = null;
    try {
      VcsFileRevision revision = git.getVcsHistoryProvider().getLastRevision(createFilePath(file));
      blame = blameForRevision(revision);
    } catch (VcsException e) {
      log.warn("Failed to blame " + file, e);
    }
    return blame;
  }

  private GitVcs getGit() {
    return GitVcs.getInstance(project);
  }

  private FilePath createFilePath(@NotNull VirtualFile file) {
    return new LocalFilePath(file.getPath(), file.isDirectory());
  }

  @Nullable
  private LensBlame blameForRevision(@Nullable VcsFileRevision revision) {
    if (revision != null && revision != VcsFileRevision.NULL) {
      return LensFileBlame.create(revision);
    }
    return null;
  }

  @Nullable
  @Override
  public LensBlame getCurrentLineBlame(@NotNull Editor editor, @NotNull VirtualFile file) {
    return lineBlameTimer.timeSupplier(() -> getCurrentLineBlameInternal(editor, file));
  }

  @Nullable
  private LensBlame getCurrentLineBlameInternal(@NotNull Editor editor, @NotNull VirtualFile file) {
    int currentLine = editor.getCaretModel().getLogicalPosition().line;
    UpToDateLineNumberProvider lineNumberProvider = new UpToDateLineNumberProviderImpl(editor.getDocument(), project);
    int correctedLine = lineNumberProvider.getLineNumber(currentLine);
    try {
      FileAnnotation annotation = getGit().getAnnotationProvider().annotate(file);
      return LensLineBlame.create(annotation, correctedLine);
    } catch (VcsException e) {
      log.warn("Failed to blame current line of " + file, e);
    }
    return null;
  }
}

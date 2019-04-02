package zielu.gittoolbox.blame.calculator;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionDataProvider;
import zielu.gittoolbox.util.GtUtil;

public class BlameCalculator {
  private final Logger log = Logger.getInstance(getClass());

  @Nullable
  public RevisionDataProvider annotate(@NotNull GitRepository repository, @NotNull VirtualFile file) {
    Project project = repository.getProject();
    GitVcs vcs = GitVcs.getInstance(project);
    VcsRevisionNumber actualRevision = vcs.getDiffProvider().getCurrentRevision(file);
    if (actualRevision == null) {
      return null;
    } else {
      GitLineHandler handler = new GitLineHandler(project, repository.getRoot(), GitCommand.BLAME);
      IncrementalBlameBuilder builder = new IncrementalBlameBuilder();
      handler.addLineListener(builder);
      handler.setStdoutSuppressed(true);
      handler.addParameters("--incremental", "-l", "-t", "-w", "--encoding=UTF-8", actualRevision.asString());
      handler.endOptions();
      handler.addRelativePaths(GtUtil.localFilePath(file));

      GitCommandResult result = Git.getInstance().runCommandWithoutCollectingOutput(handler);
      if (result.success()) {
        return new BlameRevisionDataProvider(project, builder.buildLineInfos(), file, actualRevision);
      } else if (!result.cancelled()) {
        log.warn("Blame failed:\n" + result.getErrorOutputAsJoinedString());
      }
      return null;
    }
  }
}

package zielu.gittoolbox.blame.calculator;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionDataProvider;
import zielu.gittoolbox.util.GtUtil;

public class BlameCalculator {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;
  private final BlameCalculatorLocalGateway gateway;

  public BlameCalculator(@NotNull Project project) {
    this(project, new BlameCalculatorLocalGatewayImpl(project));
  }

  // for testing
  BlameCalculator(@NotNull Project project, BlameCalculatorLocalGateway gateway) {
    this.project = project;
    this.gateway = gateway;
  }

  @Nullable
  public RevisionDataProvider annotate(@NotNull GitRepository repository, @NotNull VirtualFile file) {
    VcsRevisionNumber actualRevision = gateway.getCurrentRevisionNumber(file);
    if (actualRevision != VcsRevisionNumber.NULL) {
      GitLineHandler handler = gateway.createLineHandler(repository);
      IncrementalBlameBuilder builder = new IncrementalBlameBuilder();
      handler.addLineListener(builder);
      handler.setStdoutSuppressed(true);
      handler.addParameters("--incremental", "-l", "-t", "-w", "--encoding=UTF-8", actualRevision.asString());
      handler.endOptions();
      handler.addRelativePaths(GtUtil.localFilePath(file));

      GitCommandResult result = gateway.runCommand(handler);
      if (result.success()) {
        return new BlameRevisionDataProvider(project, builder.buildLineInfos(), file, actualRevision);
      } else if (!result.cancelled()) {
        log.warn("Blame failed:\n" + result.getErrorOutputAsJoinedString());
      }
    }
    return null;
  }
}

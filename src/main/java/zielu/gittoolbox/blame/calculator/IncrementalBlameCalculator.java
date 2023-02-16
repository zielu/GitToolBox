package zielu.gittoolbox.blame.calculator;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.repo.GitRepository;
import java.util.List;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionDataProvider;
import zielu.gittoolbox.util.GtUtil;

public class IncrementalBlameCalculator implements BlameCalculator {
  private final Logger log = Logger.getInstance(getClass());
  private final BlameCalculatorFacade facade;

  public IncrementalBlameCalculator(@NotNull Project project) {
    this(new BlameCalculatorFacade(project));
  }

  // for testing
  IncrementalBlameCalculator(BlameCalculatorFacade facade) {
    this.facade = facade;
  }

  @Nullable
  public RevisionDataProvider annotate(@NotNull GitRepository repository,
                                       @NotNull VirtualFile file,
                                       @NotNull VcsRevisionNumber revision) {
    if (revision != VcsRevisionNumber.NULL) {
      return facade.annotateTimer().timeSupplier(() -> annotateImpl(repository, file, revision));
    }
    return null;
  }

  @Nullable
  private RevisionDataProvider annotateImpl(@NotNull GitRepository repository,
                                            @NotNull VirtualFile file,
                                            @NotNull VcsRevisionNumber revision) {
    GitLineHandler handler = prepareLineHandler(repository, file, revision);
    IncrementalBlameBuilder builder = new IncrementalBlameBuilder();
    handler.addLineListener(builder);

    log.debug("Will run blame: ", handler);

    GitCommandResult result = facade.runCommand(handler);
    if (result.success()) {
      List<CommitInfo> lineInfos = builder.buildLineInfos();
      if (log.isTraceEnabled()) {
        log.trace("Blame for " + file + " is:\n" + dumpBlame(lineInfos));
      }
      return new BlameRevisionDataProvider(lineInfos, file, revision);
    } else if (!result.cancelled()) {
      log.warn("Blame failed:\n" + result.getErrorOutputAsJoinedString());
    }
    return null;
  }

  private GitLineHandler prepareLineHandler(@NotNull GitRepository repository, @NotNull VirtualFile file,
                                            @NotNull VcsRevisionNumber revisionNumber) {
    GitLineHandler handler = facade.createLineHandler(repository);
    handler.setStdoutSuppressed(true);
    handler.addParameters("--incremental", "-l", "-t", "-w", "--encoding=UTF-8", revisionNumber.asString());
    handler.endOptions();
    handler.addRelativePaths(GtUtil.localFilePath(file));
    return handler;
  }

  private StringBand dumpBlame(List<CommitInfo> lineInfos) {
    StringBand blameDump = new StringBand(lineInfos.size() * 4);
    for (int i = 0; i < lineInfos.size(); i++) {
      blameDump.append(i);
      blameDump.append(": ");
      blameDump.append(lineInfos.get(i));
      blameDump.append("\n");
    }
    return blameDump;
  }
}

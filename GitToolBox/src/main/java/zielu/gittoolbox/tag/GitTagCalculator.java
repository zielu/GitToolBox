package zielu.gittoolbox.tag;

import com.google.common.base.Preconditions;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.commands.GitLineHandlerListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class GitTagCalculator {
  private final Project project;

  private GitTagCalculator(@NotNull Project project) {
    this.project = project;
  }

  public static GitTagCalculator create(@NotNull Project project) {
    return new GitTagCalculator(Preconditions.checkNotNull(project));
  }

  public List<String> tagsForBranch(@NotNull VirtualFile gitRoot, @NotNull String branch) {
    GitLineHandler h = new GitLineHandler(project, Preconditions.checkNotNull(gitRoot), GitCommand.LOG);
    h.addParameters("--simplify-by-decoration", "--pretty=format:%d", "--encoding=UTF-8",
        Preconditions.checkNotNull(branch));
    h.setSilent(true);
    TagsLineListener tagsListener = new TagsLineListener();
    h.addLineListener(tagsListener);
    GitCommandResult result = Git.getInstance().runCommandWithoutCollectingOutput(h);
    return result.success() ? tagsListener.getTags() : Collections.emptyList();
  }

  public List<String> tagsForHead(@NotNull VirtualFile gitRoot) {
    //git tag -l --points-at HEAD
    List<String> tags = new ArrayList<>();
    GitLineHandler handler = new GitLineHandler(project, Preconditions.checkNotNull(gitRoot), GitCommand.TAG);
    handler.addParameters("-l", "--points-at", "HEAD");
    handler.setSilent(true);
    handler.addLineListener(new GitLineHandlerListener() {
      @Override
      public void onLineAvailable(String line, Key outputType) {
        if (ProcessOutputType.isStdout(outputType)) {
          String tag = StringUtils.trimToNull(line);
          if (tag != null) {
            tags.add(tag);
          }
        }
      }

      @Override
      public void processTerminated(int exitCode) {
      }

      @Override
      public void startFailed(Throwable exception) {
      }
    });
    GitCommandResult result = Git.getInstance().runCommandWithoutCollectingOutput(handler);
    return result.success() ? tags : Collections.emptyList();
  }
}

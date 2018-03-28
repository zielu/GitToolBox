package zielu.gittoolbox.tag;

import com.google.common.base.Preconditions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import java.util.Collections;
import java.util.List;
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
}

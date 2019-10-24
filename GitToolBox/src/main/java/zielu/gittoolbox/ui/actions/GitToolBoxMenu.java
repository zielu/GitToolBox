package zielu.gittoolbox.ui.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.actions.StandardVcsGroup;
import git4idea.GitVcs;
import org.jetbrains.annotations.Nullable;

public class GitToolBoxMenu extends StandardVcsGroup {

  @Override
  public AbstractVcs getVcs(Project project) {
    return GitVcs.getInstance(project);
  }

  @Nullable
  @Override
  public String getVcsName(Project project) {
    return GitVcs.NAME;
  }
}

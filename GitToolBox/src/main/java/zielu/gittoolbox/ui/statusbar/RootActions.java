package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.CalledInAwt;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.ui.statusbar.actions.RefreshStatusAction;
import zielu.gittoolbox.util.GtUtil;

/**
 * Created by Lukasz_Zielinski on 19.09.2016.
 */
public class RootActions extends DefaultActionGroup {
  private final Project project;

  public RootActions(Project project) {
    super("", true);
    this.project = project;
  }

  @CalledInAwt
  public boolean update() {
    removeAll();
    Collection<GitRepository> repositories = GitUtil.getRepositories(project);
    repositories = GtUtil.sort(repositories);
    List<GitRepository> repos = repositories.stream().filter(GtUtil::hasRemotes).collect(Collectors.toList());
    add(new RefreshStatusAction());
    if (hasRepositories(repos)) {
      addSeparator(ResBundle.message("statusBar.status.menu.repositories.title"));
      if (repos.size() == 1) {
        GitRepository repo = repos.get(0);
        addAll(StatusBarActions.actionsFor(repo));
      } else if (repos.size() > 1) {
        addAll(repos.stream().map(RepositoryActions::new).collect(Collectors.toList()));
      }
    }
    return true;
  }

  private boolean hasRepositories(Collection<GitRepository> repositories) {
    return !repositories.isEmpty();
  }

  @Override
  public boolean canBePerformed(DataContext context) {
    return true;
  }
}

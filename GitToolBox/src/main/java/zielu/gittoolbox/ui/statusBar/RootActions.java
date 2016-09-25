package zielu.gittoolbox.ui.statusBar;

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
import zielu.gittoolbox.util.GtUtil;

/**
 * Created by Lukasz_Zielinski on 19.09.2016.
 */
public class RootActions extends DefaultActionGroup {
    private final Project myProject;

    public RootActions(Project project) {
        super("", true);
        myProject = project;
    }

    @CalledInAwt
    public boolean update() {
        removeAll();
        boolean updated = false;
        Collection<GitRepository> repositories = GitUtil.getRepositories(myProject);
        repositories = GtUtil.sort(repositories);
        List<GitRepository> repos = repositories.stream().filter(GtUtil::hasRemotes).collect(Collectors.toList());
        if (repos.size() == 1) {
            updated = true;
            GitRepository repo = repos.get(0);
            addAll(StatusBarActions.actionsFor(repo));
        } else if (repos.size() > 1) {
            updated = true;
            addSeparator(ResBundle.getString("statusBar.menu.repositories.title"));
            addAll(repos.stream().map(RepositoryActions::new).collect(Collectors.toList()));
        }
        return updated;
    }

    @Override
    public boolean canBePerformed(DataContext context) {
        return true;
    }
}

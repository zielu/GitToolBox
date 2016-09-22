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
        List<RepositoryActions> actions = repositories.stream()
                                                      .filter(GtUtil::hasRemotes).map(RepositoryActions::new)
                                                      .collect(Collectors.toList());
        if (!actions.isEmpty()) {
            updated = true;
            addSeparator(ResBundle.getString("statusBar.menu.repositories.title"));
            addAll(actions);
        }
        return updated;
    }

    @Override
    public boolean canBePerformed(DataContext context) {
        return true;
    }
}

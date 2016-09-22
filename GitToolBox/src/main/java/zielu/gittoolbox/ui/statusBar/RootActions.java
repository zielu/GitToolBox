package zielu.gittoolbox.ui.statusBar;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.CalledInAwt;
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
        final AtomicBoolean updated = new AtomicBoolean();
        Collection<GitRepository> repositories = GitUtil.getRepositories(myProject);
        repositories = GtUtil.sort(repositories);
        repositories.stream().filter(GtUtil::hasRemotes).forEach(repo -> {
            addAction(new RepositoryActions(repo));
            updated.set(true);
        });
        return updated.get();
    }

    @Override
    public boolean canBePerformed(DataContext context) {
        return true;
    }
}

package zielu.gittoolbox.fetch;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import zielu.gittoolbox.ProjectAware;

public class AutoFetch implements Disposable, ProjectAware {
    private final Logger LOG = Logger.getInstance(getClass());

    private final Project myProject;

    private AutoFetch(Project project) {
        myProject = project;
    }

    public static AutoFetch create(Project project) {
        return new AutoFetch(project);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void opened() {

    }

    @Override
    public void closed() {

    }
}

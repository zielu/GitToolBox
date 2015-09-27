package zielu.gittoolbox.status;

import com.intellij.openapi.project.Project;

public class BehindWatcher {
    private final Project myProject;

    private BehindWatcher(Project project) {
        myProject = project;
    }

    public static BehindWatcher create(Project project) {
        return new BehindWatcher(project);
    }
}

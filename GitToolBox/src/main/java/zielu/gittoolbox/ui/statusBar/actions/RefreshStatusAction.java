package zielu.gittoolbox.ui.statusBar.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.ResBundle;

public class RefreshStatusAction extends DumbAwareAction {

    public RefreshStatusAction() {
        super(ResBundle.getString("refresh.status.action"));
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = getEventProject(event);
        if (project != null) {
            GitToolBoxProject gitProject = GitToolBoxProject.getInstance(project);
            gitProject.perRepoStatusCache().refreshAll();
        }
    }
}

package zielu.gittoolbox.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import zielu.gittoolbox.ui.util.AppUtil;

public class UpdateProject {
    private final Project myProject;

    private UpdateProject(Project project) {
        myProject = project;
    }

    public static UpdateProject create(Project project) {
        return new UpdateProject(project);
    }

    private void invokeAction() {
        AnAction action = ActionManager.getInstance().getAction("Vcs.UpdateProject");
        DataManager dataManager = DataManager.getInstance();
        WindowManager windowManager = WindowManager.getInstance();
        AnActionEvent evt = new AnActionEvent(null,
            dataManager.getDataContext(windowManager.getFrame(myProject)), ActionPlaces.UNKNOWN,
            action.getTemplatePresentation(), ActionManager.getInstance(), 5);
        action.actionPerformed(evt);
    }

    public void execute() {
        AppUtil.invokeLaterIfNeeded(this::invokeAction);
    }
}

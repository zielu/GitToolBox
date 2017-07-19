package zielu.gittoolbox.ui.update;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import zielu.gittoolbox.extension.UpdateProjectAction;

public class DefaultUpdateProjectAction implements UpdateProjectAction {
    @Override
    public String getId() {
        return "idea.update.project.action";
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public AnAction getAction() {
        return ActionManager.getInstance().getAction("Vcs.UpdateProject");
    }
}

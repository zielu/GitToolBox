package zielu.gittoolbox.ui.update;

import com.google.common.base.Objects;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import zielu.gittoolbox.extension.UpdateProjectAction;

public class GitExtenderUpdateProjectAction implements UpdateProjectAction {
    private final String ID = "gitextender.update.project.action";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public AnAction getAction() {
        return ActionManager.getInstance().getAction("GitExtender.UpdateAll");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdateProjectAction that = (UpdateProjectAction) o;
        return Objects.equal(ID, that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ID);
    }
}

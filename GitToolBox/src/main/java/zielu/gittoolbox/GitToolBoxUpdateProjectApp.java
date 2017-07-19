package zielu.gittoolbox;

import com.google.common.base.Preconditions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.Extensions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import zielu.gittoolbox.extension.UpdateProjectAction;
import zielu.gittoolbox.extension.UpdateProjectActionEP;

public class GitToolBoxUpdateProjectApp implements ApplicationComponent {
    private UpdateProjectAction myDefaultAction;
    private final List<UpdateProjectAction> myUpdateActions = new ArrayList<>();

    public static GitToolBoxUpdateProjectApp getInstance() {
        return ApplicationManager.getApplication().getComponent(GitToolBoxUpdateProjectApp.class);
    }

    public UpdateProjectAction getDefault() {
        return myDefaultAction;
    }

    public List<UpdateProjectAction> getAll() {
        return myUpdateActions;
    }

    public UpdateProjectAction getById(String id) {
        return myUpdateActions.stream().filter(a -> Objects.equals(id, a.getId())).findFirst().orElse(myDefaultAction);
    }

    public boolean hasId(String id) {
        return myUpdateActions.stream().anyMatch(a -> Objects.equals(id, a.getId()));
    }

    @Override
    public void initComponent() {
        List<UpdateProjectActionEP> updateProjectEPs = Arrays.asList(Extensions.getExtensions(UpdateProjectActionEP.POINT_NAME));
        updateProjectEPs.stream().map(UpdateProjectActionEP::instantiate).forEach(myUpdateActions::add);
        myUpdateActions.stream().filter(UpdateProjectAction::isDefault).findFirst().ifPresent(a -> myDefaultAction = a);
        Preconditions.checkState(myDefaultAction != null);
    }

    @Override
    public void disposeComponent() {
        myUpdateActions.clear();
        myDefaultAction = null;
    }
}

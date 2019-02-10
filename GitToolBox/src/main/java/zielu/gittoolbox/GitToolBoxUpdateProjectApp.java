package zielu.gittoolbox;

import com.google.common.base.Preconditions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.BaseComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.extension.UpdateProjectAction;
import zielu.gittoolbox.extension.UpdateProjectActionEP;

public class GitToolBoxUpdateProjectApp implements BaseComponent {
  private final List<UpdateProjectAction> updateActions = new ArrayList<>();
  private UpdateProjectAction defaultAction;

  public static GitToolBoxUpdateProjectApp getInstance() {
    return ApplicationManager.getApplication().getComponent(GitToolBoxUpdateProjectApp.class);
  }

  public UpdateProjectAction getDefault() {
    return defaultAction;
  }

  public List<UpdateProjectAction> getAll() {
    return updateActions;
  }

  public UpdateProjectAction getById(String id) {
    return updateActions.stream().filter(a -> Objects.equals(id, a.getId())).findFirst().orElse(defaultAction);
  }

  @Override
  public void initComponent() {
    List<UpdateProjectActionEP> updateProjectEPs = getUpdateProjectExtensionPoints();
    updateProjectEPs.stream().map(UpdateProjectActionEP::instantiate).forEach(updateActions::add);
    updateActions.stream().filter(UpdateProjectAction::isDefault).findFirst().ifPresent(a -> defaultAction = a);
    Preconditions.checkState(defaultAction != null);
  }

  @NotNull
  private List<UpdateProjectActionEP> getUpdateProjectExtensionPoints() {
    return UpdateProjectActionEP.POINT_NAME.getExtensionList();
  }

  @Override
  public void disposeComponent() {
    updateActions.clear();
    defaultAction = null;
  }
}

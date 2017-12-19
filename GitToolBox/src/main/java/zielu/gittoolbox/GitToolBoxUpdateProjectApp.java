package zielu.gittoolbox;

import com.google.common.base.Preconditions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.Extensions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.extension.UpdateProjectAction;
import zielu.gittoolbox.extension.UpdateProjectActionEP;

public class GitToolBoxUpdateProjectApp implements ApplicationComponent {
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

  public boolean hasId(String id) {
    return updateActions.stream().anyMatch(a -> Objects.equals(id, a.getId()));
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
    return Arrays.asList(Extensions.getExtensions(UpdateProjectActionEP.POINT_NAME));
  }

  @Override
  public void disposeComponent() {
    updateActions.clear();
    defaultAction = null;
  }
}

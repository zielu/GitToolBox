package zielu.gittoolbox;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.extension.UpdateProjectAction;
import zielu.gittoolbox.extension.UpdateProjectActionEP;
import zielu.gittoolbox.util.AppUtil;

public class GitToolBoxUpdateProjectApp {
  private final List<UpdateProjectAction> updateActions = new ArrayList<>();
  private UpdateProjectAction defaultAction;

  GitToolBoxUpdateProjectApp() {
    List<UpdateProjectActionEP> updateProjectEPs = getUpdateProjectExtensionPoints();
    updateProjectEPs.stream().map(UpdateProjectActionEP::instantiate).forEach(updateActions::add);
    updateActions.stream().filter(UpdateProjectAction::isDefault).findFirst().ifPresent(a -> defaultAction = a);
    Preconditions.checkState(defaultAction != null);
  }

  @NotNull
  private List<UpdateProjectActionEP> getUpdateProjectExtensionPoints() {
    return UpdateProjectActionEP.POINT_NAME.getExtensionList();
  }

  @NotNull
  public static GitToolBoxUpdateProjectApp getInstance() {
    return AppUtil.getServiceInstance(GitToolBoxUpdateProjectApp.class);
  }

  public List<UpdateProjectAction> getAll() {
    return updateActions;
  }

  public UpdateProjectAction getById(String id) {
    return updateActions.stream().filter(a -> Objects.equals(id, a.getId())).findFirst().orElse(defaultAction);
  }
}

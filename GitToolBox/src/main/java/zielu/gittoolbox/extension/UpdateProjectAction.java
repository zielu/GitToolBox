package zielu.gittoolbox.extension;

import com.intellij.openapi.actionSystem.AnAction;
import zielu.gittoolbox.ResBundle;

public interface UpdateProjectAction {
  String getId();

  boolean isDefault();

  default String getName() {
    return ResBundle.message(getId());
  }

  AnAction getAction();
}

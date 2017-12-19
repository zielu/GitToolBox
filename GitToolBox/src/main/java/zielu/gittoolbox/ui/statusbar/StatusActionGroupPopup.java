package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.ui.popup.PopupFactoryImpl;
import org.jetbrains.annotations.NotNull;

public class StatusActionGroupPopup extends PopupFactoryImpl.ActionGroupPopup {
  public StatusActionGroupPopup(String title, @NotNull RootActions actionGroup, @NotNull Project project,
                                Condition<AnAction> preselectActionCondition) {
    super(title, actionGroup, SimpleDataContext.getProjectContext(project),
        false, false, true, false,
        null, -1, preselectActionCondition, null);
  }
}

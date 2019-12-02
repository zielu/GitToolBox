package zielu.gittoolbox.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.awt.event.InputEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.ui.update.UpdateProjectActionService;
import zielu.gittoolbox.ui.util.AppUiUtil;

public class UpdateProject {
  private static final Logger LOG = Logger.getInstance(UpdateProject.class);
  private final Project project;

  private UpdateProject(Project project) {
    this.project = project;
  }

  public static UpdateProject create(@NotNull Project project) {
    return new UpdateProject(project);
  }

  public void execute(@NotNull AnActionEvent event) {
    AppUiUtil.invokeLaterIfNeeded(project, () -> invokeAction(event));
  }

  public void execute(@Nullable InputEvent inputEvent) {
    AppUiUtil.invokeLaterIfNeeded(project, () -> invokeAction(inputEvent));
  }

  private void invokeAction(AnActionEvent event) {
    AnAction action = getAction();
    action.actionPerformed(event);
  }

  private void invokeAction(@Nullable InputEvent inputEvent) {
    AnAction action = getAction();
    synthesiseEvent(action, inputEvent)
        .onSuccess(action::actionPerformed)
        .onError(error -> LOG.warn("Project update failed", error));
  }

  private AnAction getAction() {
    String actionId = GitToolBoxConfig2.getInstance().getUpdateProjectActionId();
    return UpdateProjectActionService.getInstance().getById(actionId).getAction();
  }

  private Promise<AnActionEvent> synthesiseEvent(@NotNull AnAction action, @Nullable InputEvent inputEvent) {
    DataManager dataManager = DataManager.getInstance();
    return dataManager.getDataContextFromFocusAsync()
        .then(dataContext -> AnActionEvent.createFromAnAction(action, inputEvent, ActionPlaces.UNKNOWN, dataContext));
  }
}

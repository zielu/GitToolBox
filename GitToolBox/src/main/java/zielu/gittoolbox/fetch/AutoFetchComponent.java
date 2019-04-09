package zielu.gittoolbox.fetch;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.util.AppUtil;

public interface AutoFetchComponent {

  void configChanged(@NotNull GitToolBoxConfigForProject previous,
                     @NotNull GitToolBoxConfigForProject current);

  void stateChanged(AutoFetchState state);

  long lastAutoFetch();

  @NotNull
  static AutoFetchComponent getInstance(@NotNull Project project) {
    return AppUtil.getComponent(project, AutoFetchComponent.class);
  }
}

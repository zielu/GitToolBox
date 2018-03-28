package zielu.gittoolbox.fetch;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;

public interface AutoFetchComponent {

  void configChanged(GitToolBoxConfigForProject config);

  void stateChanged(AutoFetchState state);

  long lastAutoFetch();

  @NotNull
  static AutoFetchComponent getInstance(@NotNull Project project) {
    return project.getComponent(AutoFetchComponent.class);
  }
}

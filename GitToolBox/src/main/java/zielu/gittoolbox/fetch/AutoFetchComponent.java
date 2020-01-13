package zielu.gittoolbox.fetch;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;
import zielu.gittoolbox.util.AppUtil;

public interface AutoFetchComponent {

  void configChanged(@NotNull GitToolBoxConfigPrj previous,
                     @NotNull GitToolBoxConfigPrj current);

  void stateChanged(AutoFetchState state);

  long lastAutoFetch();

  @NotNull
  static AutoFetchComponent getInstance(@NotNull Project project) {
    return AppUtil.getComponentInstance(project, AutoFetchComponent.class);
  }
}

package zielu.gittoolbox.fetch;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.MergedProjectConfig;
import zielu.gittoolbox.util.AppUtil;

public interface AutoFetchComponent {

  void configChanged(@NotNull MergedProjectConfig previous,
                     @NotNull MergedProjectConfig current);

  void stateChanged(AutoFetchState state);

  long lastAutoFetch();

  @NotNull
  static AutoFetchComponent getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, AutoFetchComponent.class);
  }

  void allRepositoriesInitialized(int reposCount);
}

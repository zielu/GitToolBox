package zielu.gittoolbox.fetch;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class GtFetchClientFactory {
  private GtFetchClientFactory() {
  }

  public static GtFetchClient create(@NotNull Project project) {
    return new DefaultGtFetchClient(project, true);
  }
}

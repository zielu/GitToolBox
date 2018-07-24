package zielu.gittoolbox.ui.projectview;

import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.ui.StatusPresenter;

public class NodeDecorationUi {
  protected final GitToolBoxConfig config;

  public NodeDecorationUi(@NotNull GitToolBoxConfig config) {
    this.config = config;
  }

  public StatusPresenter getPresenter() {
    return config.getPresenter();
  }

  public boolean showProjectViewLocationPath() {
    return config.showProjectViewLocationPath;
  }

  public boolean showProjectViewStatusBeforeLocation() {
    return config.showProjectViewStatusBeforeLocation;
  }

  public boolean showProjectViewHeadTags() {
    return config.showProjectViewHeadTags;
  }
}

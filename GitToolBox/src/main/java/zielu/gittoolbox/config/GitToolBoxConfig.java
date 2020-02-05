package zielu.gittoolbox.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ui.StatusPresenters;
import zielu.gittoolbox.ui.update.DefaultUpdateProjectAction;
import zielu.gittoolbox.util.AppUtil;

/**
 * Superseded by {@link GitToolBoxConfig2}.
 * @deprecated will be removed in release for 2020.1
 */
@State(
    name = "GitToolBoxAppSettings",
    storages = @Storage("git_toolbox.xml")
)
@Deprecated
public class GitToolBoxConfig implements PersistentStateComponent<GitToolBoxConfig> {
  public String presentationMode = StatusPresenters.arrows.key();
  public boolean behindTracker = true;
  public boolean showStatusWidget = true;
  public boolean showProjectViewStatus = true;
  public boolean showProjectViewLocationPath = true;
  public boolean showProjectViewStatusBeforeLocation = false;
  public boolean showProjectViewHeadTags = true;
  public String updateProjectActionId = DefaultUpdateProjectAction.ID;

  private boolean vanilla;

  @Transient
  public boolean isVanilla() {
    return vanilla;
  }

  public static GitToolBoxConfig getInstance() {
    return AppUtil.getServiceInstance(GitToolBoxConfig.class);
  }

  @Nullable
  @Override
  public GitToolBoxConfig getState() {
    return this;
  }

  @Override
  public void loadState(GitToolBoxConfig state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  @Override
  public void noStateLoaded() {
    vanilla = true;
  }
}

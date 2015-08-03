package zielu.gittoolbox;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ui.StatusPresenter;
import zielu.gittoolbox.ui.StatusPresenters;

@State(
    name = "GitToolBoxAppSettings",
    storages = {
        @Storage(
            file = StoragePathMacros.APP_CONFIG + "/git_toolbox.xml"
        )
    }
)
public class GitToolBoxConfig implements PersistentStateComponent<GitToolBoxConfig> {
    public String presentationMode = StatusPresenters.arrows.key();
    public boolean showStatusWidget = true;
    public boolean showProjectViewStatus = true;

    @Transient
    public StatusPresenter getPresenter() {
        return StatusPresenters.forKey(presentationMode);
    }

    public void setPresenter(StatusPresenter presenter) {
        presentationMode = presenter.key();
    }

    public boolean isPresenterChanged(StatusPresenter presenter) {
        return !presentationMode.equals(presenter.key());
    }

    public boolean isShowStatusWidget(boolean showStatusWidget) {
        return this.showStatusWidget != showStatusWidget;
    }

    public boolean isShowProjectViewStatus(boolean showProjectViewStatus) {
        return  this.showProjectViewStatus != showProjectViewStatus;
    }

    @Nullable
    @Override
    public GitToolBoxConfig getState() {
        return this;
    }

    public void fireChanged() {
        ApplicationManager.getApplication().getMessageBus().
            syncPublisher(GitToolBoxConfigNotifier.CONFIG_TOPIC).configChanged(this);
    }

    @Override
    public void loadState(GitToolBoxConfig state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static GitToolBoxConfig getInstance() {
        return ServiceManager.getService(GitToolBoxConfig.class);
    }
}

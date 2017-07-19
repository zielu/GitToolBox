package zielu.gittoolbox;

import com.google.common.base.Objects;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.ui.ColorUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import java.awt.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ui.StatusPresenter;
import zielu.gittoolbox.ui.StatusPresenters;
import zielu.gittoolbox.ui.update.DefaultUpdateProjectAction;

@State(
    name = "GitToolBoxAppSettings",
    storages = @Storage("git_toolbox.xml")
)
public class GitToolBoxConfig implements PersistentStateComponent<GitToolBoxConfig> {
    public String presentationMode = StatusPresenters.arrows.key();
    public boolean behindTracker = true;
    public boolean showStatusWidget = true;
    public boolean showProjectViewStatus = true;
    public boolean showProjectViewLocationPath = true;
    public boolean showProjectViewStatusBeforeLocation = false;
    public String projectViewStatusColorHex = ColorUtil.toHex(UIUtil.getInactiveTextColor());
    public boolean projectViewStatusCustomColor = false;
    public boolean projectViewStatusBold = false;
    public boolean projectViewStatusItalic = false;
    public String updateProjectActionId = DefaultUpdateProjectAction.ID;

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

    public boolean isShowStatusWidgetChanged(boolean showStatusWidget) {
        return this.showStatusWidget != showStatusWidget;
    }

    public boolean isShowProjectViewStatusChanged(boolean showProjectViewStatus) {
        return this.showProjectViewStatus != showProjectViewStatus;
    }

    public boolean isShowProjectViewLocationPathChanged(boolean showProjectViewLocationPath) {
        return this.showProjectViewLocationPath != showProjectViewLocationPath;
    }

    public boolean isShowProjectViewStatusBeforeLocationChanged(boolean showProjectViewStatusBeforeLocation) {
        return this.showProjectViewStatusBeforeLocation != showProjectViewStatusBeforeLocation;
    }

    @Transient
    @NotNull
    public Color getProjectViewStatusColor() {
        return ColorUtil.fromHex(projectViewStatusColorHex);
    }

    public void setProjectViewStatusColor(@NotNull Color color) {
        projectViewStatusColorHex = ColorUtil.toHex(color);
    }

    public boolean isProjectViewStatusColorChanged(@NotNull Color color) {
        Color current = getProjectViewStatusColor();
        return !Objects.equal(current, color);
    }

    public boolean isProjectViewStatusBoldChanged(boolean bold) {
        return projectViewStatusBold != bold;
    }

    public boolean isProjectViewStatusItalicChanged(boolean italic) {
        return projectViewStatusItalic != italic;
    }

    public boolean isProjectViewStatusCustomColorChanged(boolean enabled) {
        return projectViewStatusCustomColor != enabled;
    }

    @Transient
    public boolean isProjectViewStatusDecorated() {
        return projectViewStatusCustomColor || projectViewStatusBold || projectViewStatusItalic;
    }

    public boolean isBehindTrackerChanged(boolean behindTracker) {
        return this.behindTracker != behindTracker;
    }

    public void setUpdateProjectActionId(String id) {
        updateProjectActionId = id;
    }

    public String getUpdateProjectActionId() {
        return updateProjectActionId;
    }

    public boolean isUpdateProjectActionId(@NotNull String id) {
        return !updateProjectActionId.equals(id);
    }

    @Nullable
    @Override
    public GitToolBoxConfig getState() {
        return this;
    }

    public void fireChanged() {
        ApplicationManager.getApplication().getMessageBus().
            syncPublisher(ConfigNotifier.CONFIG_TOPIC).configChanged(this);
    }

    @Override
    public void loadState(GitToolBoxConfig state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static GitToolBoxConfig getInstance() {
        return ServiceManager.getService(GitToolBoxConfig.class);
    }
}

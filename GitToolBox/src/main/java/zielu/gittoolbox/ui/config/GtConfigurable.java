package zielu.gittoolbox.ui.config;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.GitToolBoxUpdateProjectApp;
import zielu.gittoolbox.ResBundle;

public class GtConfigurable extends GtConfigurableBase<GtForm, GitToolBoxConfig>
    implements SearchableConfigurable {

    @Nls
    @Override
    public String getDisplayName() {
        return ResBundle.getString("configurable.app.displayName");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    protected GtForm createForm() {
        return new GtForm();
    }

    @Override
    protected GitToolBoxConfig getConfig() {
        return GitToolBoxConfig.getInstance();
    }

    @Override
    protected void setFormState(GtForm form, GitToolBoxConfig config) {
        form.setPresenter(config.getPresenter());
        form.setShowGitStatus(config.showStatusWidget);
        form.setShowProjectViewStatus(config.showProjectViewStatus);
        form.setShowProjectViewLocationPath(config.showProjectViewLocationPath);
        form.setShowProjectViewStatusBeforeLocation(config.showProjectViewStatusBeforeLocation);
        form.setBehindTrackerEnabled(config.behindTracker);
        form.setProjectViewStatusColorEnabled(config.projectViewStatusCustomColor);
        form.setProjectViewStatusColor(config.getProjectViewStatusColor());
        form.setProjectViewStatusBold(config.projectViewStatusBold);
        form.setProjectViewStatusItalic(config.projectViewStatusItalic);
        form.setUpdateProjectAction(GitToolBoxUpdateProjectApp.getInstance().getById(config.getUpdateProjectActionId()));
    }

    @Override
    protected boolean checkModified(GtForm form, GitToolBoxConfig config) {
        boolean modified = config.isPresenterChanged(form.getPresenter());
        modified = modified || config.isShowStatusWidgetChanged(form.getShowGitStatus());
        modified = modified || config.isShowProjectViewStatusChanged(form.getShowProjectViewStatus());
        modified = modified || config.isShowProjectViewLocationPathChanged(form.getShowProjectViewLocationPath());
        modified = modified || config.isShowProjectViewStatusBeforeLocationChanged(form.getShowProjectViewStatusBeforeLocation());
        modified = modified || config.isBehindTrackerChanged(form.getBehindTrackerEnabled());
        modified = modified || config.isProjectViewStatusColorChanged(form.getProjectViewStatusColor());
        modified = modified || config.isProjectViewStatusCustomColorChanged(form.getProjectViewStatusColorEnabled());
        modified = modified || config.isProjectViewStatusBoldChanged(form.getProjectViewStatusBold());
        modified = modified || config.isProjectViewStatusItalicChanged(form.getProjectViewStatusItalic());
        modified = modified || config.isUpdateProjectActionId(form.getUpdateProjectAction().getId());
        return modified;
    }

    @Override
    protected void doApply(GtForm form, GitToolBoxConfig config) throws ConfigurationException {
        config.setPresenter(form.getPresenter());
        config.showStatusWidget = form.getShowGitStatus();
        config.showProjectViewStatus = form.getShowProjectViewStatus();
        config.showProjectViewLocationPath = form.getShowProjectViewLocationPath();
        config.showProjectViewStatusBeforeLocation = form.getShowProjectViewStatusBeforeLocation();
        config.behindTracker = form.getBehindTrackerEnabled();
        config.projectViewStatusCustomColor = form.getProjectViewStatusColorEnabled();
        config.setProjectViewStatusColor(form.getProjectViewStatusColor());
        config.projectViewStatusBold = form.getProjectViewStatusBold();
        config.projectViewStatusItalic = form.getProjectViewStatusItalic();
        config.updateProjectActionId = form.getUpdateProjectAction().getId();
        config.fireChanged();
    }

    @NotNull
    @Override
    public String getId() {
        return "zielu.gittoolbox.app.config";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }
}

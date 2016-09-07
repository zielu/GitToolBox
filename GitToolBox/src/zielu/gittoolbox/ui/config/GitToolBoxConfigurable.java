package zielu.gittoolbox.ui.config;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.ResBundle;

public class GitToolBoxConfigurable extends GitToolBoxConfigurableBase<GitToolBoxForm, GitToolBoxConfig>
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
    protected GitToolBoxForm createForm() {
        return new GitToolBoxForm();
    }

    @Override
    protected GitToolBoxConfig getConfig() {
        return GitToolBoxConfig.getInstance();
    }

    @Override
    protected void setFormState(GitToolBoxForm form, GitToolBoxConfig config) {
        form.setPresenter(config.getPresenter());
        form.setShowGitStatus(config.showStatusWidget);
        form.setShowProjectViewStatus(config.showProjectViewStatus);
        form.setShowProjectViewLocationPath(config.showProjectViewLocationPath);
        form.setShowProjectViewStatusBeforeLocation(config.showProjectViewStatusBeforeLocation);
        form.setBehindTrackerEnabled(config.behindTracker);
        form.afterStateSet();
    }

    @Override
    protected boolean checkModified(GitToolBoxForm form, GitToolBoxConfig config) {
        boolean modified = config.isPresenterChanged(form.getPresenter());
        modified = modified || config.isShowStatusWidgetChanged(form.getShowGitStatus());
        modified = modified || config.isShowProjectViewStatusChanged(form.getShowProjectViewStatus());
        modified = modified || config.isShowProjectViewLocationPathChanged(form.getShowProjectViewLocationPath());
        modified = modified || config.isShowProjectViewStatusBeforeLocationChanged(form.getShowProjectViewStatusBeforeLocation());
        modified = modified || config.isBehindTrackerChanged(form.getBehindTrackerEnabled());
        return modified;
    }

    @Override
    protected void doApply(GitToolBoxForm form, GitToolBoxConfig config) throws ConfigurationException {
        config.setPresenter(form.getPresenter());
        config.showStatusWidget = form.getShowGitStatus();
        config.showProjectViewStatus = form.getShowProjectViewStatus();
        config.showProjectViewLocationPath = form.getShowProjectViewLocationPath();
        config.showProjectViewStatusBeforeLocation = form.getShowProjectViewStatusBeforeLocation();
        config.behindTracker = form.getBehindTrackerEnabled();
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

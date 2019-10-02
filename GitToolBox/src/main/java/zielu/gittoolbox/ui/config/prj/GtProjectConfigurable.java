package zielu.gittoolbox.ui.config.prj;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.help.HelpKey;
import zielu.intellij.ui.GtConfigurableBase;

public class GtProjectConfigurable extends GtConfigurableBase<GtPrjForm, GitToolBoxConfigForProject> implements
    SearchableConfigurable {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;

  public GtProjectConfigurable(@NotNull Project project) {
    this.project = project;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return ResBundle.message("configurable.prj.displayName");
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return HelpKey.PROJECT_CONFIG.getId();
  }

  @Override
  protected GtPrjForm createForm() {
    return new GtPrjForm();
  }

  @Override
  protected void afterInit(GtPrjForm form) {
    form.setProject(project);
    form.afterInit();
  }

  @Override
  protected GitToolBoxConfigForProject getConfig() {
    return GitToolBoxConfigForProject.getInstance(project);
  }

  @Override
  protected void setFormState(GtPrjForm form, GitToolBoxConfigForProject config) {
    log.debug("Set form state");
    form.setAutoFetchEnabled(config.autoFetch);
    form.setAutoFetchInterval(config.autoFetchIntervalMinutes);
    form.setCommitCompletionEnabled(config.commitDialogCompletion);
    form.setCommitCompletionConfigs(config.completionConfigs);
    form.setAutoFetchExclusions(config.autoFetchExclusionConfigs);
    form.setAutoFetchOnBranchSwitchEnabled(config.autoFetchOnBranchSwitch);
    form.setReferencePointConfig(config.referencePointForStatus);
  }

  @Override
  protected boolean checkModified(GtPrjForm form, GitToolBoxConfigForProject config) {
    boolean modified = config.isAutoFetchChanged(form.getAutoFetchEnabled());
    modified = modified || config.isAutoFetchIntervalMinutesChanged(form.getAutoFetchInterval());
    modified = modified || config.isCommitDialogCompletionChanged(form.getCommitCompletionEnabled());
    modified = modified || config.isCommitDialogCompletionConfigsChanged(form.getCommitCompletionConfigs());
    modified = modified || config.isAutoFetchExclusionConfigsChanged(form.getAutoFetchExclusions());
    modified = modified || config.isAutoFetchOnBranchSwitchChanged(form.getAutoFetchOnBranchSwitchEnabled());
    modified = modified || config.isReferencePointForStatusChanged(form.getReferencePointConfig());
    log.debug("Modified: ", modified);
    return modified;
  }

  @Override
  protected void doApply(GtPrjForm form, GitToolBoxConfigForProject config) throws ConfigurationException {
    final GitToolBoxConfigForProject previousConfig = config.copy();

    config.autoFetch = form.getAutoFetchEnabled();
    config.autoFetchIntervalMinutes = form.getAutoFetchInterval();
    config.commitDialogCompletion = form.getCommitCompletionEnabled();
    config.completionConfigs = form.getCommitCompletionConfigs();
    config.autoFetchExclusionConfigs = form.getAutoFetchExclusions();
    config.autoFetchOnBranchSwitch = form.getAutoFetchOnBranchSwitchEnabled();
    config.referencePointForStatus = form.getReferencePointConfig();

    config.fireChanged(project, previousConfig);
    log.debug("Applied");
  }

  @NotNull
  @Override
  public String getId() {
    return "zielu.gittoolbox.prj.config";
  }

  @Nullable
  @Override
  public Runnable enableSearch(String option) {
    return null;
  }
}

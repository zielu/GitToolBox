package zielu.gittoolbox.ui.config.prj;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;
import zielu.gittoolbox.config.ProjectConfig;
import zielu.gittoolbox.help.HelpKey;
import zielu.intellij.ui.ConfigUiBinder;
import zielu.intellij.ui.GtBinderConfigurableBase;

public class GtProjectConfigurable extends GtBinderConfigurableBase<GtPrjForm, GitToolBoxConfigPrj> implements
    SearchableConfigurable {
  private final Project project;

  protected GtProjectConfigurable(@NotNull Project project) {
    this.project = project;
  }

  @Override
  protected void bind(ConfigUiBinder<GitToolBoxConfigPrj, GtPrjForm> binder) {
    binder.bind(GitToolBoxConfigPrj::getAutoFetch,
        GitToolBoxConfigPrj::setAutoFetch,
        GtPrjForm::getAutoFetchEnabled,
        GtPrjForm::setAutoFetchEnabled);
    binder.bind(GitToolBoxConfigPrj::getAutoFetchIntervalMinutes,
        GitToolBoxConfigPrj::setAutoFetchIntervalMinutes,
        GtPrjForm::getAutoFetchInterval,
        GtPrjForm::setAutoFetchInterval);
    binder.bind(GitToolBoxConfigPrj::getAutoFetchExclusionConfigs,
        GitToolBoxConfigPrj::setAutoFetchExclusionConfigs,
        GtPrjForm::getAutoFetchExclusions,
        GtPrjForm::setAutoFetchExclusions);
    binder.bind(GitToolBoxConfigPrj::getAutoFetchOnBranchSwitch,
        GitToolBoxConfigPrj::setAutoFetchOnBranchSwitch,
        GtPrjForm::getAutoFetchOnBranchSwitchEnabled,
        GtPrjForm::setAutoFetchOnBranchSwitchEnabled);
    binder.bind(GitToolBoxConfigPrj::getCommitDialogCompletion,
        GitToolBoxConfigPrj::setCommitDialogCompletion,
        GtPrjForm::getCommitCompletionEnabled,
        GtPrjForm::setCommitCompletionEnabled);
    binder.bind(GitToolBoxConfigPrj::getCompletionConfigs,
        GitToolBoxConfigPrj::setCompletionConfigs,
        GtPrjForm::getCommitCompletionConfigs,
        GtPrjForm::setCommitCompletionConfigs);
    binder.bind(GitToolBoxConfigPrj::getReferencePointForStatus,
        GitToolBoxConfigPrj::setReferencePointForStatus,
        GtPrjForm::getReferencePointConfig,
        GtPrjForm::setReferencePointConfig);
    binder.bind(GitToolBoxConfigPrj::getCommitMessageValidation,
        GitToolBoxConfigPrj::setCommitMessageValidation,
        GtPrjForm::getCommitMessageValidationEnabled,
        GtPrjForm::setCommitMessageValidationEnabled);
    binder.bind(GitToolBoxConfigPrj::getCommitMessageValidationRegex,
        GitToolBoxConfigPrj::setCommitMessageValidationRegex,
        GtPrjForm::getCommitMessageValidationRegex,
        GtPrjForm::setCommitMessageValidationRegex);
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
  protected GitToolBoxConfigPrj getConfig() {
    return ProjectConfig.get(project);
  }

  @Override
  protected GitToolBoxConfigPrj copy(@NotNull GitToolBoxConfigPrj config) {
    return config.copy();
  }

  @Override
  protected void afterApply(GitToolBoxConfigPrj previous, GitToolBoxConfigPrj current) {
    ProjectConfig.getInstance(project).updateState(current);
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

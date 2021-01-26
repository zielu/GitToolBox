package zielu.gittoolbox.ui.config.app;

import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.config.AppConfig;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.help.HelpKey;
import zielu.intellij.ui.ConfigUiBinder;
import zielu.intellij.ui.GtBinderConfigurableBase;

public class GtConfigurable extends GtBinderConfigurableBase<GtForm, GitToolBoxConfig2>
    implements SearchableConfigurable {

  @Nls
  @Override
  public String getDisplayName() {
    return ResBundle.message("configurable.app.displayName");
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return HelpKey.APP_CONFIG.getId();
  }

  @Override
  protected GtForm createForm() {
    return new GtForm();
  }

  @Override
  protected GitToolBoxConfig2 getConfig() {
    return AppConfig.getConfig();
  }

  @Override
  protected void bind(ConfigUiBinder<GitToolBoxConfig2, GtForm> binder) {
    binder.bind(GitToolBoxConfig2::getPresenter,
        GitToolBoxConfig2::setPresenter,
        GtForm::getPresenter,
        GtForm::setPresenter);
    binder.bind(GitToolBoxConfig2::getShowStatusWidget,
        GitToolBoxConfig2::setShowStatusWidget,
        GtForm::getShowGitStatus,
        GtForm::setShowGitStatus);
    binder.bind(GitToolBoxConfig2::getShowProjectViewStatus,
        GitToolBoxConfig2::setShowProjectViewStatus,
        GtForm::getShowProjectViewStatus,
        GtForm::setShowProjectViewStatus);
    binder.bind(GitToolBoxConfig2::getBehindTracker,
        GitToolBoxConfig2::setBehindTracker,
        GtForm::getBehindTrackerEnabled,
        GtForm::setBehindTrackerEnabled);
    binder.bind(GitToolBoxConfig2::getUpdateProjectAction,
        GitToolBoxConfig2::setUpdateProjectAction,
        GtForm::getUpdateProjectAction,
        GtForm::setUpdateProjectAction);
    binder.bind(GitToolBoxConfig2::getDecorationParts,
        GitToolBoxConfig2::setDecorationParts,
        GtForm::getDecorationParts,
        GtForm::setDecorationParts);
    binder.bind(GitToolBoxConfig2::getShowBlameWidget,
        GitToolBoxConfig2::setShowBlameWidget,
        GtForm::getShowStatusBlame,
        GtForm::setShowStatusBlame);
    binder.bind(GitToolBoxConfig2::getShowEditorInlineBlame,
        GitToolBoxConfig2::setShowEditorInlineBlame,
        GtForm::getShowEditorInlineBlame,
        GtForm::setShowEditorInlineBlame);
    binder.bind(GitToolBoxConfig2::getCommitDialogCompletionMode,
        GitToolBoxConfig2::setCommitDialogCompletionMode,
        GtForm::getCommitDialogCompletionMode,
        GtForm::setCommitDialogCompletionMode);
    binder.bind(GitToolBoxConfig2::getCommitDialogGitmojiCompletion,
        GitToolBoxConfig2::setCommitDialogGitmojiCompletion,
        GtForm::getCommitDialogGitmojiCompletionEnabled,
        GtForm::setCommitDialogGitmojiCompletionEnabled);
    binder.bind(GitToolBoxConfig2::getCommitDialogGitmojiCompletionUnicode,
        GitToolBoxConfig2::setCommitDialogGitmojiCompletionUnicode,
        GtForm::getCommitDialogGitmojiCompletionUnicode,
        GtForm::setCommitDialogGitmojiCompletionUnicode);
    binder.bind(GitToolBoxConfig2::getBlameInlineAuthorNameType,
        GitToolBoxConfig2::setBlameInlineAuthorNameType,
        GtForm::getBlameInlineAuthorNameType,
        GtForm::setBlameInlineAuthorNameType);
    binder.bind(GitToolBoxConfig2::getBlameInlineDateType,
        GitToolBoxConfig2::setBlameInlineDateType,
        GtForm::getBlameDateType,
        GtForm::setBlameDateType);
    binder.bind(GitToolBoxConfig2::getBlameInlineShowSubject,
        GitToolBoxConfig2::setBlameInlineShowSubject,
        GtForm::getBlameShowSubject,
        GtForm::setBlameShowSubject);
    binder.bind(GitToolBoxConfig2::getBlameStatusAuthorNameType,
        GitToolBoxConfig2::setBlameStatusAuthorNameType,
        GtForm::getBlameStatusAuthorNameType,
        GtForm::setBlameStatusAuthorNameType);
    binder.bind(GitToolBoxConfig2::getAlwaysShowInlineBlameWhileDebugging,
        GitToolBoxConfig2::setAlwaysShowInlineBlameWhileDebugging,
        GtForm::alwaysShowInlineBlameWhileDebugging);
    binder.bind(GitToolBoxConfig2::getAbsoluteDateTimeStyle,
        GitToolBoxConfig2::setAbsoluteDateTimeStyle,
        GtForm::getAbsoluteDateTimeStyle,
        GtForm::setAbsoluteDateTimeStyle);
    binder.bind(GitToolBoxConfig2::getShowChangesInStatusBar,
        GitToolBoxConfig2::setShowChangesInStatusBar,
        GtForm::getShowChangesInStatusBar,
        GtForm::setShowChangesInStatusBar);

    binder.bind(config -> config.getExtrasConfig().getAutoFetchEnabledOverride().getEnabled(),
        (config, value) -> config.getExtrasConfig().getAutoFetchEnabledOverride().setEnabled(value),
        GtForm::getAutoFetchEnabledOverride,
        GtForm::setAutoFetchEnabledOverride
    );
    binder.bind(config -> config.getExtrasConfig().getAutoFetchEnabledOverride().getValue(),
        (config, value) -> config.getExtrasConfig().getAutoFetchEnabledOverride().setValue(value),
        GtForm::getAutoFetchEnabled,
        GtForm::setAutoFetchEnabled
    );
    binder.bind(config -> config.getExtrasConfig().getAutoFetchEnabledOverride().getAppliedPaths(),
        GtForm::setAppliedAutoFetchEnabledPaths
    );

    binder.bind(config -> config.getExtrasConfig().getAutoFetchOnBranchSwitchOverride().getEnabled(),
        (config, value) -> config.getExtrasConfig().getAutoFetchOnBranchSwitchOverride().setEnabled(value),
        GtForm::getAutoFetchOnBranchSwitchEnabledOverride,
        GtForm::setAutoFetchOnBranchSwitchEnabledOverride
    );
    binder.bind(config -> config.getExtrasConfig().getAutoFetchOnBranchSwitchOverride().getValue(),
        (config, value) -> config.getExtrasConfig().getAutoFetchOnBranchSwitchOverride().setValue(value),
        GtForm::getAutoFetchOnBranchSwitchEnabled,
        GtForm::setAutoFetchOnBranchSwitchEnabled
    );
    binder.bind(config -> config.getExtrasConfig().getAutoFetchOnBranchSwitchOverride().getAppliedPaths(),
        GtForm::setAppliedAutoFetchOnBranchSwitchEnabledPaths
    );
  }

  @NotNull
  @Override
  protected GitToolBoxConfig2 copy(@NotNull GitToolBoxConfig2 config) {
    return config.copy();
  }

  @Override
  protected void storeConfig(@NotNull GitToolBoxConfig2 existing, @NotNull GitToolBoxConfig2 updated) {
    AppConfig.getInstance().updateState(updated);
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

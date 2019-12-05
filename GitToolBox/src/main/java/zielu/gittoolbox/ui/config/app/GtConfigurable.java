package zielu.gittoolbox.ui.config.app;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.help.HelpKey;
import zielu.gittoolbox.ui.update.UpdateProjectActionService;
import zielu.intellij.ui.GtConfigurableBase;

public class GtConfigurable extends GtConfigurableBase<GtForm, GitToolBoxConfig2>
    implements SearchableConfigurable {
  private final Logger log = Logger.getInstance(getClass());

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
    return GitToolBoxConfig2.getInstance();
  }

  @Override
  protected void setFormState(GtForm form, GitToolBoxConfig2 config) {
    log.debug("Set form state");
    form.setPresenter(config.getPresenter());
    form.setShowGitStatus(config.showStatusWidget);
    form.setShowProjectViewStatus(config.showProjectViewStatus);
    form.setBehindTrackerEnabled(config.behindTracker);
    form.setUpdateProjectAction(UpdateProjectActionService.getInstance().getById(config.getUpdateProjectActionId()));
    form.setDecorationParts(config.decorationParts);
    form.setShowStatusBlame(config.showBlameWidget);
    form.setShowEditorInlineBlame(config.showEditorInlineBlame);
    form.setCommitDialogCompletionMode(config.commitDialogCompletionMode);
    form.setBlameInlineAuthorNameType(config.blameInlineAuthorNameType);
    form.setBlameDateType(config.blameInlineDateType);
    form.setBlameShowSubject(config.blameInlineShowSubject);
    form.setBlameStatusAuthorNameType(config.blameStatusAuthorNameType);
    form.setAbsoluteDateTimeStyle(config.absoluteDateTimeStyle);
    form.setShowChangesInStatusBar(config.showChangesInStatusBar);
  }

  @Override
  protected boolean checkModified(GtForm form, GitToolBoxConfig2 config) {
    boolean modified = config.isPresenterChanged(form.getPresenter());
    modified = modified || config.isShowStatusWidgetChanged(form.getShowGitStatus());
    modified = modified || config.isShowProjectViewStatusChanged(form.getShowProjectViewStatus());
    modified = modified || config.isBehindTrackerChanged(form.getBehindTrackerEnabled());
    modified = modified || config.isUpdateProjectActionId(form.getUpdateProjectAction().getId());
    modified = modified || config.isDecorationPartsChanged(form.getDecorationParts());
    modified = modified || config.isShowBlameWidgetChanged(form.getShowStatusBlame());
    modified = modified || config.isShowEditorInlineBlameChanged(form.getShowEditorInlineBlame());
    modified = modified || config.isCommitDialogCompletionModeChanged(form.getCommitDialogCompletionMode());
    modified = modified || config.isBlameInlineAuthorNameTypeChanged(form.getBlameInlineAuthorNameType());
    modified = modified || config.isBlameInlineDateTypeChanged(form.getBlameDateType());
    modified = modified || config.isBlameInlineShowSubjectChanged(form.getBlameShowSubject());
    modified = modified || config.isBlameStatusAuthorNameTypeChanged(form.getBlameStatusAuthorNameType());
    modified = modified || config.isAbsoluteDateTimeStyleChanged(form.getAbsoluteDateTimeStyle());
    modified = modified || config.isShowChangesInStatusBarChanged(form.getShowChangesInStatusBar());
    log.debug("Modified: ", modified);
    return modified;
  }

  @Override
  protected void doApply(GtForm form, GitToolBoxConfig2 config) {
    final GitToolBoxConfig2 previousConfig = config.copy();

    config.setPresenter(form.getPresenter());
    config.showStatusWidget = form.getShowGitStatus();
    config.showProjectViewStatus = form.getShowProjectViewStatus();
    config.decorationParts = form.getDecorationParts();
    config.behindTracker = form.getBehindTrackerEnabled();
    config.updateProjectActionId = form.getUpdateProjectAction().getId();
    config.decorationParts = form.getDecorationParts();
    config.showBlameWidget = form.getShowStatusBlame();
    config.showEditorInlineBlame = form.getShowEditorInlineBlame();
    config.commitDialogCompletionMode = form.getCommitDialogCompletionMode();
    config.blameInlineAuthorNameType = form.getBlameInlineAuthorNameType();
    config.blameInlineDateType = form.getBlameDateType();
    config.blameInlineShowSubject = form.getBlameShowSubject();
    config.blameStatusAuthorNameType = form.getBlameStatusAuthorNameType();
    config.absoluteDateTimeStyle = form.getAbsoluteDateTimeStyle();
    config.showChangesInStatusBar = form.getShowChangesInStatusBar();

    //Mark migrated here to handle case when config is modified without opening a project
    //Example: from launch dialog
    config.previousVersionMigrated = true;

    config.fireChanged(previousConfig);
    log.debug("Applied");
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

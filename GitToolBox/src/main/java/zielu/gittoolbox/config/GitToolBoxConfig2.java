package zielu.gittoolbox.config;

import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ui.StatusPresenter;
import zielu.gittoolbox.ui.StatusPresenters;
import zielu.gittoolbox.ui.update.DefaultUpdateProjectAction;

@State(
    name = "GitToolBoxAppSettings2",
    storages = @Storage("git_toolbox_2.xml")
)
public class GitToolBoxConfig2 implements PersistentStateComponent<GitToolBoxConfig2> {
  public String presentationMode = StatusPresenters.arrows.key();
  public boolean behindTracker = true;
  public boolean showStatusWidget = true;
  public boolean showProjectViewStatus = true;
  public boolean showBlame = true;
  public boolean showEditorInlineBlame = false;
  public String updateProjectActionId = DefaultUpdateProjectAction.ID;
  public List<DecorationPartConfig> decorationParts = Lists.newArrayList(
      DecorationPartConfig.builder().withType(DecorationPartType.LOCATION).withPrefix("- ").build(),
      DecorationPartConfig.builder().withType(DecorationPartType.BRANCH).build(),
      DecorationPartConfig.builder().withType(DecorationPartType.STATUS).build(),
      DecorationPartConfig.builder().withType(DecorationPartType.TAGS_ON_HEAD).withPrefix("(").withPostfix(")").build()
  );
  public CommitCompletionMode commitDialogCompletionMode = CommitCompletionMode.AUTOMATIC;
  public boolean experimentalBlameEditorCaching = true;
  public AuthorNameType blameInlineAuthorNameType = AuthorNameType.LASTNAME;
  public DateType blameInlineDateType = DateType.AUTO;
  public boolean blameInlineShowSubject = true;

  public boolean previousVersionMigrated;

  public static GitToolBoxConfig2 getInstance() {
    return ServiceManager.getService(GitToolBoxConfig2.class);
  }


  public GitToolBoxConfig2 copy() {
    GitToolBoxConfig2 copy = new GitToolBoxConfig2();
    copy.presentationMode = presentationMode;
    copy.behindTracker = behindTracker;
    copy.showStatusWidget = showStatusWidget;
    copy.showProjectViewStatus = showProjectViewStatus;
    copy.showBlame = showBlame;
    copy.showEditorInlineBlame = showEditorInlineBlame;
    copy.updateProjectActionId = updateProjectActionId;
    copy.decorationParts = decorationParts.stream().map(DecorationPartConfig::copy).collect(Collectors.toList());
    copy.previousVersionMigrated = previousVersionMigrated;
    return copy;
  }

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

  public boolean isBehindTrackerChanged(boolean behindTracker) {
    return this.behindTracker != behindTracker;
  }

  public String getUpdateProjectActionId() {
    return updateProjectActionId;
  }

  public void setUpdateProjectActionId(String id) {
    updateProjectActionId = id;
  }

  public boolean isUpdateProjectActionId(@NotNull String id) {
    return !updateProjectActionId.equals(id);
  }

  public boolean isDecorationPartsChanged(List<DecorationPartConfig> decorationParts) {
    return !this.decorationParts.equals(decorationParts);
  }

  public boolean isShowBlameChanged(boolean showBlame) {
    return this.showBlame != showBlame;
  }

  public boolean isShowEditorInlineBlameChanged(boolean showEditorInlineBlame) {
    return this.showEditorInlineBlame != showEditorInlineBlame;
  }

  public boolean isCommitDialogCompletionModeChanged(CommitCompletionMode commitDialogCompletionMode) {
    return this.commitDialogCompletionMode != commitDialogCompletionMode;
  }

  public boolean isExperimentalBlameEditorCachingChanged(boolean experimentalBlameEditorCaching) {
    return this.experimentalBlameEditorCaching != experimentalBlameEditorCaching;
  }

  public boolean isBlameInlineAuthorNameTypeChanged(AuthorNameType blameAuthorNameType) {
    return this.blameInlineAuthorNameType != blameAuthorNameType;
  }

  public boolean isBlameInlineDateTypeChanged(DateType blameDateType) {
    return this.blameInlineDateType != blameDateType;
  }

  public boolean isBlameInlineShowSubjectChanged(boolean blameInlineShowSubject) {
    return this.blameInlineShowSubject != blameInlineShowSubject;
  }

  public boolean isBlameInlinePresentationChanged(GitToolBoxConfig2 other) {
    return isBlameInlineAuthorNameTypeChanged(other.blameInlineAuthorNameType)
        || isBlameInlineDateTypeChanged(other.blameInlineDateType)
        || isBlameInlineShowSubjectChanged(other.blameInlineShowSubject);
  }

  @Nullable
  @Override
  public GitToolBoxConfig2 getState() {
    return this;
  }

  public void fireChanged(@NotNull GitToolBoxConfig2 previousConfig) {
    ApplicationManager.getApplication().getMessageBus().syncPublisher(ConfigNotifier.CONFIG_TOPIC)
        .configChanged(previousConfig, this);
  }

  @Override
  public void loadState(@NotNull GitToolBoxConfig2 state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}

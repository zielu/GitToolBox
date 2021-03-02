package zielu.gittoolbox.config

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.extension.update.UpdateProjectAction
import zielu.gittoolbox.fetch.AutoFetchParams
import zielu.gittoolbox.ui.StatusPresenter
import zielu.gittoolbox.ui.StatusPresenters
import zielu.gittoolbox.ui.update.DefaultUpdateProjectAction
import zielu.gittoolbox.ui.update.UpdateProjectActionService

internal data class GitToolBoxConfig2(
  var presentationMode: String = StatusPresenters.arrows.key(),
  var behindTracker: Boolean = true,
  var showStatusWidget: Boolean = true,
  var showProjectViewStatus: Boolean = true,
  var showBlameWidget: Boolean = true,
  var showEditorInlineBlame: Boolean = true,
  var updateProjectActionId: String = DefaultUpdateProjectAction.ID,
  var commitDialogCompletionMode: CommitCompletionMode = CommitCompletionMode.AUTOMATIC,
  var blameInlineAuthorNameType: AuthorNameType = AuthorNameType.LASTNAME,
  var blameStatusAuthorNameType: AuthorNameType = AuthorNameType.LASTNAME,
  var blameInlineDateType: DateType = DateType.AUTO,
  var blameInlineShowSubject: Boolean = true,
  var absoluteDateTimeStyle: AbsoluteDateTimeStyle = AbsoluteDateTimeStyle.FROM_LOCALE,
  var showChangesInStatusBar: Boolean = true,
  var previousVersionMigrated: Boolean = false,
  var version: Int = 1,
  var decorationParts: List<DecorationPartConfig> = ConfigDefaults.decorationParts(),
  @Deprecated("Since 201.4.0") var extrasConfig: ExtrasConfig = ExtrasConfig(),
  var commitDialogGitmojiCompletion: Boolean = false,
  var commitDialogGitmojiCompletionUnicode: Boolean = false,
  @Deprecated("Since 193.8.1") var hideInlineBlameWhileDebugging: Boolean = true,
  var alwaysShowInlineBlameWhileDebugging: Boolean = false,
  var autoFetchEnabled: Boolean = true,
  var autoFetchIntervalMinutes: Int = AutoFetchParams.DEFAULT_INTERVAL_MINUTES,
  var autoFetchOnBranchSwitch: Boolean = true,
  var commitDialogCompletion: Boolean = true,
  var completionConfigs: List<CommitCompletionConfig> = arrayListOf(CommitCompletionConfig()),
  var referencePointForStatus: ReferencePointForStatusConfig = ReferencePointForStatusConfig(),
  var commitMessageValidationEnabled: Boolean = false,
  var commitMessageValidationRegex: String = "(?:fix|chore|docs|feat|refactor|style|test)(?:\\(.*\\))?: [A-Z].*\\s#\\d+"
) {

  @Transient
  fun copy(): GitToolBoxConfig2 {
    return GitToolBoxConfig2(
      presentationMode,
      behindTracker,
      showStatusWidget,
      showProjectViewStatus,
      showBlameWidget,
      showEditorInlineBlame,
      updateProjectActionId,
      commitDialogCompletionMode,
      blameInlineAuthorNameType,
      blameStatusAuthorNameType,
      blameInlineDateType,
      blameInlineShowSubject,
      absoluteDateTimeStyle,
      showChangesInStatusBar,
      previousVersionMigrated,
      version,
      decorationParts.map { it.copy() },
      extrasConfig.copy(),
      commitDialogGitmojiCompletion,
      commitDialogGitmojiCompletionUnicode,
      hideInlineBlameWhileDebugging,
      alwaysShowInlineBlameWhileDebugging,
      autoFetchEnabled,
      autoFetchIntervalMinutes,
      autoFetchOnBranchSwitch,
      commitDialogCompletion,
      completionConfigs.map { it.copy() },
      referencePointForStatus,
      commitMessageValidationEnabled,
      commitMessageValidationRegex
    )
  }

  @Transient
  fun getPresenter(): StatusPresenter {
    return StatusPresenters.forKey(presentationMode)
  }

  fun setPresenter(presenter: StatusPresenter) {
    presentationMode = presenter.key()
  }

  @Transient
  fun getUpdateProjectAction(): UpdateProjectAction {
    return UpdateProjectActionService.getInstance().getById(updateProjectActionId)
  }

  fun setUpdateProjectAction(action: UpdateProjectAction) {
    updateProjectActionId = action.getId()
  }

  fun isBlameInlinePresentationChanged(other: GitToolBoxConfig2): Boolean {
    return blameInlineAuthorNameType != other.blameInlineAuthorNameType ||
      blameInlineDateType != other.blameInlineDateType ||
      blameInlineShowSubject != other.blameInlineShowSubject ||
      absoluteDateTimeStyle != other.absoluteDateTimeStyle
  }

  fun isBlameStatusPresentationChanged(other: GitToolBoxConfig2): Boolean {
    return blameStatusAuthorNameType != other.blameStatusAuthorNameType ||
      absoluteDateTimeStyle != other.absoluteDateTimeStyle
  }

  @Transient
  fun isChangesTrackingEnabled(): Boolean {
    return showChangesInStatusBar || decorationParts.stream()
      .anyMatch { part: DecorationPartConfig -> part.type == DecorationPartType.CHANGED_COUNT }
  }
}

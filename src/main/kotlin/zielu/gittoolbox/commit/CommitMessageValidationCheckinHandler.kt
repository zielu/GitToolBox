package zielu.gittoolbox.commit

import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.ui.BooleanCommitOption
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.ui.RefreshableOnComponent
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.compat.GitCompatUtil
import zielu.gittoolbox.config.MergedProjectConfig
import zielu.gittoolbox.config.ProjectConfig
import zielu.intellij.ui.YesNoDialog

internal class CommitMessageValidationCheckinHandler(
  private val checkinPanel: CheckinProjectPanel
) : CheckinHandler() {
  private val config: MergedProjectConfig
    get() = ProjectConfig.getMerged(checkinPanel.project)

  override fun getBeforeCheckinConfigurationPanel(): RefreshableOnComponent {
    return BooleanCommitOption(
      checkinPanel,
      ResBundle.message("commit.message.validation.label"),
      false,
      { config.commitMessageValidation() },
      { config.setCommitMessageValidation(it) }
    )
  }

  override fun beforeCheckin(): ReturnResult {
    return if (shouldValidate()) {
      validate()
    } else {
      ReturnResult.COMMIT
    }
  }

  private fun shouldValidate(): Boolean {
    return config.commitMessageValidation() && hasModificationsUnderGit()
  }

  private fun hasModificationsUnderGit(): Boolean {
    return GitCompatUtil.getRepositoriesForFiles(checkinPanel.project, checkinPanel.files).isNotEmpty()
  }

  private fun validate(): ReturnResult {
    if (checkinPanel.commitMessage.matches(Regex(config.commitMessageValidationRegex()))) {
      return ReturnResult.COMMIT
    }
    val confirmationDialog = YesNoDialog(
      checkinPanel.project,
      checkinPanel.preferredFocusedComponent,
      ResBundle.message("commit.message.validation.dialog.title"),
      ResBundle.message("commit.message.validation.dialog.body")
    )
    confirmationDialog.makeCancelDefault()
    val commit = confirmationDialog.showAndGet()
    return if (commit) {
      ReturnResult.COMMIT
    } else ReturnResult.CANCEL
  }
}

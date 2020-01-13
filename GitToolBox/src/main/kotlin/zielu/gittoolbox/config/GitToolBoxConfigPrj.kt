package zielu.gittoolbox.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.fetch.AutoFetchParams
import zielu.gittoolbox.formatter.Formatter
import zielu.gittoolbox.util.AppUtil

@State(name = "GitToolBoxProjectSettings", storages = [Storage("git_toolbox_prj.xml")])
internal data class GitToolBoxConfigPrj(
  var autoFetch: Boolean = true,
  var autoFetchIntervalMinutes: Int = AutoFetchParams.DEFAULT_INTERVAL_MINUTES,
  @Deprecated("Since 192.3.1") var autoFetchExclusions: List<String> = ArrayList(),
  var autoFetchExclusionConfigs: List<AutoFetchExclusionConfig> = ArrayList(),
  var autoFetchOnBranchSwitch: Boolean = true,
  var commitDialogCompletion: Boolean = true,
  var completionConfigs: List<CommitCompletionConfig> = arrayListOf(CommitCompletionConfig()),
  var referencePointForStatus: ReferencePointForStatusConfig = ReferencePointForStatusConfig(),
  var commitMessageValidation: Boolean = false,
  var commitMessageValidationRegex: String = "(?:fix|chore|docs|feat|refactor|style|test)(?:\\(.*\\))?: [A-Z].*\\s#\\d+"
) : PersistentStateComponent<GitToolBoxConfigPrj> {

  companion object {
    @JvmStatic
    fun getInstance(project: Project): GitToolBoxConfigPrj {
      return AppUtil.getServiceInstance(project, GitToolBoxConfigPrj::class.java)
    }
  }

  @Transient
  fun copy(): GitToolBoxConfigPrj {
    return GitToolBoxConfigPrj(
      autoFetch,
      autoFetchIntervalMinutes,
      autoFetchExclusions,
      autoFetchExclusionConfigs.map { it.copy() },
      autoFetchOnBranchSwitch,
      commitDialogCompletion,
      completionConfigs.map { it.copy() },
      referencePointForStatus.copy(),
      commitMessageValidation,
      commitMessageValidationRegex
    )
  }

  @Transient
  fun getCompletionFormatters(): List<Formatter> {
    return completionConfigs.map { it.createFormatter() }
  }

  fun isReferencePointForStatusChanged(other: GitToolBoxConfigPrj): Boolean {
    return referencePointForStatus != other.referencePointForStatus
  }

  fun fireChanged(project: Project, previous: GitToolBoxConfigPrj) {
    project.messageBus.syncPublisher(ProjectConfigNotifier.CONFIG_TOPIC).configChanged(previous, this)
  }

  override fun getState(): GitToolBoxConfigPrj? {
    return this
  }

  override fun loadState(state: GitToolBoxConfigPrj) {
    XmlSerializerUtil.copyBean(state, this)
  }
}

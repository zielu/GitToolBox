package zielu.gittoolbox.config.override

import zielu.gittoolbox.config.CommitCompletionConfig
import zielu.gittoolbox.config.ConfigItem

internal data class CommitCompletionConfigListOverride(
  var enabled: Boolean = false,
  var values: List<CommitCompletionConfig> = arrayListOf()
) : ConfigItem<CommitCompletionConfigListOverride> {

  override fun copy(): CommitCompletionConfigListOverride {
    return CommitCompletionConfigListOverride(
      enabled,
      values.map { it.copy() }
    )
  }
}

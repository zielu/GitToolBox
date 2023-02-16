package zielu.gittoolbox.ui.config

import zielu.gittoolbox.config.CommitCompletionConfig

internal class CommitCompletionConfigFormData(
  private val config: CommitCompletionConfig
) : GtPatternFormatterData {
  override var pattern: String
    get() = config.pattern
    set(value) { config.pattern = value }
  override var testInput: String
    get() = config.testInput
    set(value) { config.testInput = value }
}

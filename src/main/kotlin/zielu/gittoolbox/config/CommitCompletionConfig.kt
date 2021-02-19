package zielu.gittoolbox.config

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.formatter.Formatter
import zielu.gittoolbox.formatter.RegExpFormatter
import zielu.gittoolbox.formatter.SimpleFormatter

internal data class CommitCompletionConfig(
  var type: CommitCompletionType = CommitCompletionType.SIMPLE,
  var pattern: String = "",
  var testInput: String = ""
) : ConfigItem<CommitCompletionConfig> {

  override fun copy(): CommitCompletionConfig {
    return CommitCompletionConfig(
      type,
      pattern,
      testInput
    )
  }

  @Transient
  fun getFormatter(): Formatter {
    return when (type) {
      CommitCompletionType.SIMPLE -> {
        SimpleFormatter
      }
      CommitCompletionType.PATTERN -> {
        RegExpFormatter.create(pattern)
      }
    }
  }

  @Transient
  fun getPresentableText(): String {
    return when (type) {
      CommitCompletionType.SIMPLE -> {
        "Branch name"
      }
      CommitCompletionType.PATTERN -> {
        pattern
      }
    }
  }

  companion object {
    @JvmStatic
    fun createDefault(type: CommitCompletionType): CommitCompletionConfig {
      return when (type) {
        CommitCompletionType.SIMPLE -> {
          CommitCompletionConfig()
        }
        CommitCompletionType.PATTERN -> {
          CommitCompletionConfig(
            type,
            "(.*)",
            "test input"
          )
        }
      }
    }

    @JvmStatic
    fun createIssuePattern(): CommitCompletionConfig {
      return CommitCompletionConfig(
        CommitCompletionType.PATTERN,
        "(\\w+-\\d+).*",
        "JIRA4PRJ-1234_branch_name"
      )
    }
  }
}

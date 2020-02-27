package zielu.gittoolbox.completion

import com.google.common.collect.ImmutableMap
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.project.Project
import com.intellij.util.ProcessingContext
import zielu.gittoolbox.config.CommitCompletionMode
import zielu.gittoolbox.config.GitToolBoxConfig2.Companion.getInstance
import zielu.gittoolbox.config.GitToolBoxConfigPrj

internal abstract class CompletionProviderBase : CompletionProvider<CompletionParameters>() {
  private val modeHandlers: Map<CommitCompletionMode, (parameters: CompletionParameters) -> Boolean> = ImmutableMap
    .builder<CommitCompletionMode, (parameters: CompletionParameters) -> Boolean>()
    .put(CommitCompletionMode.AUTOMATIC) { true }
    .put(CommitCompletionMode.ON_DEMAND) { parameters: CompletionParameters -> !parameters.isAutoPopup }
    .build()

  override fun addCompletions(
    parameters: CompletionParameters,
    context: ProcessingContext,
    result: CompletionResultSet
  ) {
    if (shouldComplete(parameters)) {
      setupCompletions(getProject(parameters), result)
    }
  }

  protected abstract fun setupCompletions(project: Project, result: CompletionResultSet)

  private fun shouldComplete(parameters: CompletionParameters): Boolean {
    val config = getConfig(getProject(parameters))
    if (config.commitDialogCompletion) {
      val mode = getInstance().commitDialogCompletionMode
      val modeHandler = modeHandlers.getOrDefault(mode) { params: CompletionParameters -> true }
      return modeHandler.invoke(parameters)
    }
    return false
  }

  private fun getConfig(project: Project): GitToolBoxConfigPrj {
    return GitToolBoxConfigPrj.getInstance(project)
  }

  private fun getProject(parameters: CompletionParameters): Project {
    return parameters.position.project
  }
}

package zielu.gittoolbox.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import git4idea.branch.GitBranchUtil
import git4idea.repo.GitRepository
import zielu.gittoolbox.formatter.Formatter
import zielu.gittoolbox.util.GtUtil.name

internal class CurrentBranchCompletionProvider : CompletionProviderBase() {

  override fun setupCompletions(project: Project, result: CompletionResultSet) {
    val completionService = CompletionService.getInstance(project)
    val formatters = completionService.getFormatters()
    val branchInfos = getBranchInfo(completionService)
    log.debug("Setup completions for: ", branchInfos)
    branchInfos.forEach { branchInfo: Pair<String, String> ->
      val branchName = branchInfo.getFirst()
      formatters.forEach { formatter: Formatter ->
        addCompletion(
          result,
          formatter,
          branchName,
          branchInfo.getSecond()
        )
      }
    }
  }

  private fun addCompletion(result: CompletionResultSet, formatter: Formatter, branchName: String, repoName: String) {
    val formatted = formatter.format(branchName)
    if (formatted.getDisplayable()) {
      result.addElement(
        LookupElementBuilder.create(formatted.text)
          .withTypeText(repoName, true)
          .withIcon(formatter.icon)
      )
    } else {
      log.debug("Skipped completion: ", formatted)
    }
  }

  private fun getBranchInfo(completionService: CompletionService): Collection<Pair<String, String>> {
    return completionService.getAffected().map { getGitRepositoryNames(it) }
  }

  private fun getGitRepositoryNames(repo: GitRepository): Pair<String, String> {
    return Pair.create(GitBranchUtil.getDisplayableBranchText(repo), name(repo))
  }

  private companion object {
    private val log = Logger.getInstance(CurrentBranchCompletionProvider::class.java)
  }
}

package zielu.gittoolbox.completion

import com.google.common.collect.ImmutableList
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.compat.GitCompatUtil
import zielu.gittoolbox.config.ProjectConfig
import zielu.gittoolbox.formatter.Formatter
import zielu.gittoolbox.util.BaseFacade
import java.io.File

internal class CompletionFacade(private val project: Project) : BaseFacade(project) {
  fun getFormatters(): ImmutableList<Formatter> {
    return ImmutableList.copyOf(ProjectConfig.getMerged(project).commitDialogCompletionFormatters())
  }

  fun getRepositories(files: Collection<File>): Collection<GitRepository> {
    return getMetrics().timer("completion-get-repos")
      .timeSupplierKt { GitCompatUtil.getRepositoriesForFiles(project, files) }
  }
}

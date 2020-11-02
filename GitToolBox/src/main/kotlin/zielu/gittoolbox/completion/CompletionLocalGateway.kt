package zielu.gittoolbox.completion

import com.google.common.collect.ImmutableList
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.compat.GitCompatUtil
import zielu.gittoolbox.config.ProjectConfig
import zielu.gittoolbox.formatter.Formatter
import zielu.gittoolbox.util.LocalGateway
import java.io.File

internal class CompletionLocalGateway(private val project: Project) : LocalGateway(project) {
  fun getFormatters(): ImmutableList<Formatter> {
    return ImmutableList.copyOf(ProjectConfig.get(project).getCompletionFormatters())
  }

  fun getRepositories(files: Collection<File>): Collection<GitRepository> {
    return getMetrics().timer("completion-get-repos")
      .timeSupplierKt { GitCompatUtil.getRepositoriesForFiles(project, files) }
  }
}

package zielu.gittoolbox.completion

import com.google.common.collect.ImmutableList
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.compat.GitCompatUtil
import zielu.gittoolbox.config.GitToolBoxConfigPrj
import zielu.gittoolbox.formatter.Formatter
import zielu.gittoolbox.util.LocalGateway
import java.io.File

internal class CompletionLocalGateway(private val project: Project) : LocalGateway(project) {

  fun getFormatters(): ImmutableList<Formatter> {
    return ImmutableList.copyOf(GitToolBoxConfigPrj.getInstance(project).getCompletionFormatters())
  }

  fun getRepositories(files: Collection<File>): Collection<GitRepository> {
    return getMetrics().timer("completion-get-repos")
      .timeSupplier { GitCompatUtil.getRepositoriesForFiles(project, files) }
  }
}

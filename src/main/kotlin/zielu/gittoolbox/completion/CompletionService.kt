package zielu.gittoolbox.completion

import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.formatter.Formatter
import zielu.gittoolbox.util.AppUtil.getExistingServiceInstance
import zielu.gittoolbox.util.AppUtil.getServiceInstance
import java.util.Optional

internal interface CompletionService {
  fun setScopeProvider(scopeProvider: CompletionScopeProvider)

  fun getAffected(): Collection<GitRepository>

  fun getFormatters(): List<Formatter>

  companion object {
    fun getInstance(project: Project): CompletionService {
      return getServiceInstance(project, CompletionService::class.java)
    }

    fun getExistingInstance(project: Project): Optional<CompletionService> {
      return getExistingServiceInstance(project, CompletionService::class.java)
    }
  }
}

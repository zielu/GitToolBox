package zielu.gittoolbox.fetch

import com.intellij.openapi.project.Project
import git4idea.fetch.GitFetchResult
import git4idea.fetch.GitFetchSupport
import git4idea.repo.GitRepository

internal object GtFetchUtil {
  /**
   * Fetch for single repository.
   * Taken from [git4idea.actions.GitFetch]
   *
   * @param repository repository to fetch
   */
  @JvmStatic
  fun fetch(repository: GitRepository): GitFetchResult {
    val project = repository.project
    val fetchSupport = GitFetchSupport.fetchSupport(project)
    return fetchSupport.fetchAllRemotes(setOf(repository))
  }

  @JvmStatic
  fun fetch(project: Project, repositories: Collection<GitRepository>): GitFetchResult {
    val fetchSupport = GitFetchSupport.fetchSupport(project)
    return fetchSupport.fetchAllRemotes(repositories)
  }
}

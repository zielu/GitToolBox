package zielu.gittoolbox.completion

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.formatter.Formatter
import java.io.File
import java.lang.ref.WeakReference

internal class CompletionServiceImpl(
  project: Project
) : CompletionService, Disposable {
  private val facade: CompletionFacade = CompletionFacade(project)
  private var scopeProviderRef: WeakReference<CompletionScopeProvider>? = null
  @Volatile
  private var formatters: List<Formatter>? = null

  override fun setScopeProvider(scopeProvider: CompletionScopeProvider) {
    log.debug("Set scope provider: ", scopeProvider)
    scopeProviderRef = WeakReference(scopeProvider)
    clearFormatters()
  }

  private fun clearFormatters() {
    synchronized(this) {
      formatters = null
      log.info("Clear formatters")
    }
  }

  override fun getAffected(): Collection<GitRepository> {
    val scopeProvider = getScopeProvider()
    val affectedFiles = scopeProvider.getAffectedFiles()
    log.debug("Get affected files: ", affectedFiles)
    val affectedRepositories = findAffectedRepositories(affectedFiles)
    log.debug("Get affected repositories: ", affectedRepositories)
    return affectedRepositories
  }

  private fun getScopeProvider(): CompletionScopeProvider {
    return scopeProviderRef?.get() ?: CompletionScopeProvider.empty
  }

  private fun findAffectedRepositories(affectedFiles: Collection<File>): Collection<GitRepository> {
    return facade.getRepositories(affectedFiles)
  }

  override fun getFormatters(): List<Formatter> {
    if (formatters == null) {
      synchronized(this) {
        if (formatters == null) {
          formatters = facade.getFormatters()
          log.debug("Current formatters: ", formatters)
        }
      }
    }
    return formatters!!
  }

  override fun dispose() {
    clearFormatters()
    scopeProviderRef = null
  }

  private companion object {
    private val log = Logger.getInstance(CompletionServiceImpl::class.java)
  }
}

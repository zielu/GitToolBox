package zielu.gittoolbox.blame

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import zielu.gittoolbox.blame.calculator.CachingBlameCalculator
import zielu.intellij.util.ZDisposeGuard

internal class BlameLoaderImpl(val project: Project) : BlameLoader, Disposable {
  private val disposeGuard = ZDisposeGuard()
  private val gateway = BlameLoaderLocalGateway(project)
  private val calculator = CachingBlameCalculator(project)

  init {
    gateway.registerDisposable(this, calculator)
    gateway.registerDisposable(this, disposeGuard)
  }

  override fun annotate(file: VirtualFile): BlameAnnotation {
    if (disposeGuard.isActive()) {
      val repo = gateway.getRepoForFile(file)
      if (disposeGuard.isActive()) {
        if (repo != null) {
          val fileRevision = gateway.getCurrentRevisionNumber(file)
          val provider = calculator.annotate(repo, file, fileRevision)
          if (disposeGuard.isActive() && provider != null) {
            return BlameAnnotationImpl(provider, gateway.getRevisionService())
          }
        } else {
          log.debug("File is not under Git root: ", file)
        }
      }
    }
    return BlameAnnotation.EMPTY
  }

  override fun getCurrentRevision(repository: GitRepository): VcsRevisionNumber {
    return if (disposeGuard.isActive()) {
      gateway.getCurrentRevisionNumber(repository)
    } else {
      VcsRevisionNumber.NULL
    }
  }

  override fun invalidateForRoot(root: VirtualFile) {
    if (disposeGuard.isActive()) {
      calculator.invalidateForRoot(root)
    }
  }

  override fun dispose() {
    // do nothing
  }

  private companion object {
    private val log = Logger.getInstance(BlameLoaderImpl::class.java)
  }
}

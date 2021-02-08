package zielu.gittoolbox.blame

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.impl.UpToDateLineNumberProviderImpl
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.revision.RevisionInfo
import zielu.gittoolbox.util.BaseFacade
import zielu.intellij.util.ZDisposeGuard

internal class BlameServiceFacade(private var project: Project) : BaseFacade(project), Disposable {
  private val disposeGuard = ZDisposeGuard()

  fun lineNumberProvider(document: Document): UpToDateLineNumberProvider {
    return UpToDateLineNumberProviderImpl(document, project)
  }

  fun fireBlameUpdated(vFile: VirtualFile) {
    publishAsync(this) { it.syncPublisher(BlameService.BLAME_UPDATE).blameUpdated(vFile) }
  }

  fun fireBlameInvalidated(vFile: VirtualFile) {
    publishAsync(this) { it.syncPublisher(BlameService.BLAME_UPDATE).blameInvalidated(vFile) }
  }

  fun getAnnotation(vFile: VirtualFile): BlameAnnotation {
    return if (disposeGuard.isActive()) {
      BlameCache.getInstance(project).getAnnotation(vFile)
    } else {
      BlameAnnotation.EMPTY
    }
  }

  fun timeLineBlame(blame: () -> RevisionInfo): RevisionInfo {
    return if (disposeGuard.isActive()) {
      getMetrics().timer("blame-document-line").timeSupplierKt(blame)
    } else {
      RevisionInfo.NULL
    }
  }

  override fun dispose() {
    dispose(disposeGuard)
  }
}

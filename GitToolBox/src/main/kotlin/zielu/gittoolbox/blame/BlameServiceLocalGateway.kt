package zielu.gittoolbox.blame

import com.codahale.metrics.Timer
import com.intellij.openapi.editor.Document
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.impl.UpToDateLineNumberProviderImpl
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.util.LocalGateway

internal class BlameServiceLocalGateway(private var project: Project) : LocalGateway(project) {
  fun lineNumberProvider(document: Document): UpToDateLineNumberProvider {
    return UpToDateLineNumberProviderImpl(document, project)
  }

  fun fireBlameUpdated(vFile: VirtualFile) {
    publishAsync { it.syncPublisher(BlameService.BLAME_UPDATE).blameUpdated(vFile) }
  }

  fun fireBlameInvalidated(vFile: VirtualFile) {
    publishAsync { it.syncPublisher(BlameService.BLAME_UPDATE).blameInvalidated(vFile) }
  }

  fun getAnnotation(vFile: VirtualFile): BlameAnnotation {
    return BlameCache.getInstance(project).getAnnotation(vFile)
  }

  fun getLineBlameTimer(): Timer {
    return getMetrics().timer("blame-document-line")
  }
}

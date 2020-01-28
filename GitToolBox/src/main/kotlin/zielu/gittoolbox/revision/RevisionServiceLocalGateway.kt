package zielu.gittoolbox.revision

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcs.log.impl.HashImpl
import com.intellij.vcs.log.impl.VcsProjectLog
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.util.LocalGateway

internal class RevisionServiceLocalGateway(private val project: Project) : LocalGateway(project) {
  fun loadCommitMessage(vFile: VirtualFile, revisionNumber: VcsRevisionNumber): String {
    val timer = getMetrics().timer("commitMessageCache.load")
    return timer.timeSupplier(loadCommitMessageImpl(vFile, revisionNumber))
  }

  private fun loadCommitMessageImpl(vFile: VirtualFile, revisionNumber: VcsRevisionNumber): () -> String {
    val root = rootForFile(vFile)
    if (root != null) {
      return {
        loadCommitMessageFromIndex(revisionNumber, root) ?: ""
      }
    } else {
      return { "" }
    }
  }

  private fun rootForFile(file: VirtualFile): VirtualFile? {
    return VirtualFileRepoCache.getInstance(project).getRepoRootForFile(file)
  }

  private fun loadCommitMessageFromIndex(revisionNumber: VcsRevisionNumber, root: VirtualFile): String? {
    val logManager = VcsProjectLog.getInstance(project).logManager
    if (logManager != null) {
      val dataManager = logManager.dataManager
      val getter = dataManager.index.dataGetter
      if (getter != null) {
        val commitIndex = dataManager.getCommitIndex(HashImpl.build(revisionNumber.asString()), root)
        if (commitIndex > -1) {
          return getter.getFullMessage(commitIndex)
        }
      }
    }
    return null
  }

  fun registerMessagesSizeGauge(gauge: () -> Long) {
    getMetrics().gauge("commitMessageCache.size", gauge)
  }
}

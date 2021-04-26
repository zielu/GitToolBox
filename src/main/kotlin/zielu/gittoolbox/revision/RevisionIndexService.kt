package zielu.gittoolbox.revision

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcs.log.Hash
import com.intellij.vcs.log.impl.VcsProjectLog
import zielu.gittoolbox.util.AppUtil

internal class RevisionIndexService(
  private val project: Project
) {

  fun getAccessor(hash: Hash, root: VirtualFile): RevisionIndexAccessor? {
    val logManager = VcsProjectLog.getInstance(project).logManager
    if (logManager != null) {
      val dataManager = logManager.dataManager
      val getter = dataManager.index.dataGetter
      if (getter != null) {
        val commitIndex = dataManager.getCommitIndex(hash, root)
        if (commitIndex > -1) {
          return RevisionIndexAccessor(getter, commitIndex)
        }
      }
    }
    return null
  }

  companion object {
    fun getInstance(project: Project): RevisionIndexService {
      return AppUtil.getServiceInstance(project, RevisionIndexService::class.java)
    }
  }
}

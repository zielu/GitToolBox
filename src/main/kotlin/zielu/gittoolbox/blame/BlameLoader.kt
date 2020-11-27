package zielu.gittoolbox.blame

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import zielu.gittoolbox.util.AppUtil
import java.util.Optional

internal interface BlameLoader {
  @Throws(VcsException::class)
  fun annotate(file: VirtualFile): BlameAnnotation

  fun getCurrentRevision(repository: GitRepository): VcsRevisionNumber

  fun invalidateForRoot(root: VirtualFile)

  companion object {
    @JvmStatic
    fun getInstance(project: Project): BlameLoader {
      return AppUtil.getServiceInstance(project, BlameLoader::class.java)
    }

    @JvmStatic
    fun getExistingInstance(project: Project): Optional<BlameLoader> {
      return AppUtil.getExistingServiceInstance(project, BlameLoader::class.java)
    }
  }
}

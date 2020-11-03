package zielu.gittoolbox.blame

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository

internal object NullBlameLoader : BlameLoader {
  override fun annotate(file: VirtualFile): BlameAnnotation {
    return BlameAnnotation.EMPTY
  }

  override fun getCurrentRevision(repository: GitRepository): VcsRevisionNumber {
    return VcsRevisionNumber.NULL
  }

  override fun invalidateForRoot(root: VirtualFile) {
    // do nothing
  }
}

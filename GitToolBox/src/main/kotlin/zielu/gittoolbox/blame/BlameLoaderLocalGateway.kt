package zielu.gittoolbox.blame

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitVcs
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.revision.RevisionService

class BlameLoaderLocalGateway(private val project: Project) {
  fun getRepoForFile(vFile: VirtualFile): GitRepository? {
    return VirtualFileRepoCache.getInstance(project).getRepoForFile(vFile)
  }

  fun getRevisionService(): RevisionService {
    return RevisionService.getInstance(project)
  }

  fun getGitVcs(): GitVcs {
    return GitVcs.getInstance(project)
  }
}

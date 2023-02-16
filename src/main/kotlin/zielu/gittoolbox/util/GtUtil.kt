package zielu.gittoolbox.util

import com.intellij.dvcs.DvcsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.LocalFilePath
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.vcs.log.Hash
import com.intellij.vcs.log.impl.HashImpl
import git4idea.GitUtil
import git4idea.GitVcs
import git4idea.config.GitVcsSettings
import git4idea.repo.GitRepository
import org.apache.commons.lang3.ObjectUtils

internal object GtUtil {
  @JvmStatic
  fun name(repository: GitRepository): String {
    return DvcsUtil.getShortRepositoryName(repository)
  }

  fun vcs(repository: GitRepository): GitVcs {
    return repository.vcs
  }

  @JvmStatic
  fun hash(hash: String): Hash {
    return HashImpl.build(hash)
  }

  @JvmStatic
  fun hasRemotes(repository: GitRepository): Boolean {
    return !repository.remotes.isEmpty()
  }

  @JvmStatic
  fun getRepositories(project: Project): List<GitRepository> {
    return ArrayList(GitUtil.getRepositories(project))
  }

  @JvmStatic
  fun sort(repositories: Collection<GitRepository>): List<GitRepository> {
    return DvcsUtil.sortRepositories(ArrayList(repositories))
  }

  @RequiresEdt
  fun guessCurrentRepository(project: Project): GitRepository? {
    val repositoryManager = GitUtil.getRepositoryManager(project)
    val recentRootPath = GitVcsSettings.getInstance(project).recentRootPath
    return DvcsUtil.guessCurrentRepositoryQuick(project, repositoryManager, recentRootPath)
  }

  @RequiresEdt
  fun getCurrentRepository(project: Project): GitRepository? {
    val repositoryManager = GitUtil.getRepositoryManager(project)
    return DvcsUtil.guessCurrentRepositoryQuick(project, repositoryManager, null)
  }

  @JvmStatic
  fun findFileByUrl(url: String): VirtualFile? {
    return VirtualFileManager.getInstance().findFileByUrl(url)
  }

  @JvmStatic
  fun localFilePath(file: VirtualFile): FilePath {
    return LocalFilePath(file.path, file.isDirectory)
  }

  fun hasGitVcs(project: Project): Boolean {
    val vcsManager = ProjectLevelVcsManager.getInstance(project)
    return vcsManager.findVcsByName(GitVcs.NAME) != null
  }

  fun getCurrentRevision(project: Project, file: VirtualFile): VcsRevisionNumber {
    val diffProvider = GitVcs.getInstance(project).diffProvider
    val currentRevision = diffProvider.getCurrentRevision(file)
    return ObjectUtils.defaultIfNull<VcsRevisionNumber>(currentRevision, VcsRevisionNumber.NULL)
  }
}

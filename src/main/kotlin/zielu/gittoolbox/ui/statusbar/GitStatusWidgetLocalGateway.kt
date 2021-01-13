package zielu.gittoolbox.ui.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.PerRepoInfoCache
import zielu.gittoolbox.cache.RepoInfo
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.ui.ExtendedRepoInfo
import zielu.gittoolbox.ui.ExtendedRepoInfoService

internal class GitStatusWidgetLocalGateway {

  fun getRepoForFile(project: Project, file: VirtualFile): GitRepository? {
    return VirtualFileRepoCache.getInstance(project).getRepoForFile(file)
  }

  fun getRepoInfo(repo: GitRepository): RepoInfo {
    return PerRepoInfoCache.getInstance(repo.project).getInfo(repo)
  }

  fun getRepoInfos(project: Project): List<RepoInfo> {
    return PerRepoInfoCache.getInstance(project).getAllInfos()
  }

  fun getExtendedRepoInfo(repo: GitRepository): ExtendedRepoInfo {
    return ExtendedRepoInfoService.getInstance().getExtendedRepoInfo(repo)
  }

  fun getExtendedRepoInfo(project: Project): ExtendedRepoInfo {
    return ExtendedRepoInfoService.getInstance().getExtendedRepoInfo(project)
  }

  fun getIsVisibleConfig(): Boolean {
    return AppConfig.getConfig().showStatusWidget
  }
}

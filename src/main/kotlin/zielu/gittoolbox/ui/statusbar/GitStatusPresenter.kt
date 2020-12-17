package zielu.gittoolbox.ui.statusbar

import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import org.apache.commons.lang3.StringUtils
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.ResBundle.message
import zielu.gittoolbox.ResBundle.na
import zielu.gittoolbox.ResIcons.ChangesPresent
import zielu.gittoolbox.ResIcons.NoChanges
import zielu.gittoolbox.cache.RepoInfo
import zielu.gittoolbox.cache.RepoStatus
import zielu.gittoolbox.config.AppConfig.Companion.getConfig
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.status.GitAheadBehindCount
import zielu.gittoolbox.ui.ExtendedRepoInfo
import zielu.gittoolbox.ui.StatusText.format
import java.util.ArrayList
import javax.swing.Icon

internal class GitStatusPresenter(project: Project) {
  private val toolTip = StatusToolTip(project)
  private var text: String? = null
  private var icon: Icon? = null

  fun getIcon(): Icon? = icon

  fun getText(): String? = text

  fun getToolTipText(): String? = null

  fun empty() {
    toolTip.clear()
    text = ""
    icon = null
  }

  fun disabled() {
    toolTip.clear()
    text = message("status.prefix") + " " + ResBundle.disabled()
    icon = null
  }

  fun updateData(
    repository: GitRepository,
    repoInfo: RepoInfo,
    extendedInfo: ExtendedRepoInfo
  ) {
    icon = null
    val config = getConfig()
    updateIcon(config, extendedInfo)

    val parts: MutableList<String> = ArrayList()
    format(config, extendedInfo, parts)

    toolTip.update(repository, null)

    format(config, repoInfo, parts)
    if (config.showStatusWidget) {
      toolTip.update(repository, repoInfo.count())
    }

    text = combineParts(parts)
  }

  private fun updateIcon(config: GitToolBoxConfig2, extendedInfo: ExtendedRepoInfo) {
    if (config.showChangesInStatusBar) {
      if (extendedInfo.hasChanged()) {
        icon = if (extendedInfo.changedCount.value > 0) {
          ChangesPresent
        } else {
          NoChanges
        }
      }
    }
  }

  private fun format(config: GitToolBoxConfig2, extendedInfo: ExtendedRepoInfo, parts: MutableList<String>) {
    if (config.showChangesInStatusBar) {
      parts.add(format(extendedInfo))
    }
  }

  private fun format(config: GitToolBoxConfig2, repoInfo: RepoInfo, parts: MutableList<String>) {
    if (config.showStatusWidget) {
      val count = repoInfo.count()
      if (count == null) {
        parts.add(na())
      } else {
        parts.add(format(count))
      }
    }
  }

  private fun combineParts(parts: Collection<String>): String {
    return parts
      .filter { StringUtils.isNotBlank(it) }
      .joinToString("/")
  }

  fun updateData(repoInfos: List<RepoInfo>, extendedInfo: ExtendedRepoInfo) {
    if (repoInfos.isEmpty()) {
      empty()
    }
    icon = null
    val config = getConfig()
    updateIcon(config, extendedInfo)

    val parts: MutableList<String> = ArrayList()
    format(config, extendedInfo, parts)

    toolTip.clear()

    format(config, combine(repoInfos), parts)

    text = combineParts(parts)
  }

  private fun combine(repoInfos: List<RepoInfo>): RepoInfo {
    if (repoInfos.size == 1) {
      return repoInfos[0]
    }
    val aheadBehind = repoInfos
      .mapNotNull { it.count() }
      .filter { it.status().isValid() }
      .reduce(this::combine)

    return if (aheadBehind.isNotZero) {
      RepoInfo.create(RepoStatus.empty(), aheadBehind, emptyList())
    } else {
      RepoInfo.empty()
    }
  }

  private fun combine(first: GitAheadBehindCount, second: GitAheadBehindCount): GitAheadBehindCount {
    val ahead = first.ahead.value() + second.ahead.value()
    val behind = second.behind.value() + second.behind.value()
    return GitAheadBehindCount.success(ahead, null, behind, null)
  }
}

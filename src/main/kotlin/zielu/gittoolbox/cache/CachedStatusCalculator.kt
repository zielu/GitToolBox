package zielu.gittoolbox.cache

import com.intellij.openapi.diagnostic.Logger
import com.intellij.vcs.log.Hash
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.RepoInfo.Companion.create
import zielu.gittoolbox.status.GitStatusCalculator
import zielu.gittoolbox.tag.GitTagCalculator
import zielu.gittoolbox.util.GtUtil.name
import zielu.intellij.metrics.Metrics

internal class CachedStatusCalculator(metricsSupplier: () -> Metrics) {
  private val metrics: Metrics by lazy {
    metricsSupplier.invoke()
  }

  fun update(
    repo: GitRepository,
    calculator: GitStatusCalculator,
    status: RepoStatus
  ): RepoInfo {
    val statusUpdateTimer = metrics.timer("status-update")
    val count = statusUpdateTimer
      .timeSupplierKt {
        calculator.aheadBehindStatus(
          repo,
          status.localHash(),
          status.parentHash()
        )
      }
    val repoName = name(repo)
    if (!status.sameHashes(count)) {
      log.info("Hash mismatch for repo [$repoName] between count and status: $count <> $status")
    }
    val tagsUpdateTimer = metrics.timer("tags-update")
    val tags = tagsUpdateTimer.timeSupplierKt { calculateTags(repo, status.localHash()) }
    log.info("Found tags for repo [$repoName]: $tags")
    return create(status, count, tags)
  }

  private fun calculateTags(
    repository: GitRepository,
    localHash: Hash?
  ): List<String> {
    val tagCalculator = GitTagCalculator.create(repository.project)
    return if (localHash == null) {
      tagCalculator.tagsForHead(repository.root)
    } else {
      tagCalculator.tagsForCommit(repository.root, localHash)
    }
  }

  private companion object {
    private val log = Logger.getInstance(CachedStatusCalculator::class.java)
  }
}

package zielu.gittoolbox.cache;

import com.codahale.metrics.Timer;
import com.intellij.openapi.diagnostic.Logger;
import git4idea.repo.GitRepository;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.GitStatusCalculator;
import zielu.gittoolbox.tag.GitTagCalculator;
import zielu.gittoolbox.util.GtUtil;

class CachedStatusCalculator {
  private final Logger log = Logger.getInstance(getClass());
  private final Metrics metrics;

  CachedStatusCalculator(Metrics metrics) {
    this.metrics = metrics;
  }

  public RepoInfo update(@NotNull GitRepository repo, @NotNull GitStatusCalculator calculator,
                         @NotNull RepoStatus currentStatus) {
    return updateStatus(repo, calculator, currentStatus);
  }

  private RepoInfo updateStatus(@NotNull GitRepository repo, @NotNull GitStatusCalculator calculator,
                            RepoStatus status) {
    Timer statusUpdateLatency = metrics.timer("status-update");
    GitAheadBehindCount count = statusUpdateLatency
        .timeSupplier(() -> calculator.aheadBehindStatus(repo, status.localHash(), status.parentHash()));
    log.debug("Calculated status [", GtUtil.name(repo), "]: ", count);
    if (!status.sameHashes(count)) {
      log.warn("Hash mismatch between count and status: " + count + " <> " + status);
    }
    GitTagCalculator tagCalculator = GitTagCalculator.create(repo.getProject());
    List<String> tags = Optional.ofNullable(status.localHash())
        .map(hash -> tagCalculator.tagsForCommit(repo.getRoot(), hash))
        .orElseGet(() -> tagCalculator.tagsForHead(repo.getRoot()));
    return RepoInfo.create(status, count, tags);
  }
}

package zielu.gittoolbox.cache;

import com.codahale.metrics.Timer;
import com.intellij.openapi.diagnostic.Logger;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.metrics.MetricsHost;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.GitStatusCalculator;
import zielu.gittoolbox.tag.GitTagCalculator;
import zielu.gittoolbox.util.GtUtil;

import java.util.List;

public class CachedStatusCalculator {
  private final Logger log = Logger.getInstance(getClass());

  public RepoInfo update(@NotNull GitRepository repo, @NotNull GitStatusCalculator calculator,
                         @NotNull RepoStatus currentStatus) {
    return updateStatus(repo, calculator, currentStatus);
  }

  private RepoInfo updateStatus(@NotNull GitRepository repo, @NotNull GitStatusCalculator calculator,
                            RepoStatus status) {
    Timer statusUpdateLatency = MetricsHost.project(repo.getProject()).timer("status-update");
    GitAheadBehindCount count = statusUpdateLatency
        .timeSupplier(() -> calculator.aheadBehindStatus(repo, status.localHash(), status.remoteHash()));
    log.debug("Calculated status [", GtUtil.name(repo), "]: ", count);
    if (!status.sameHashes(count)) {
      log.warn("Hash mismatch between count and status: " + count + " <> " + status);
    }
    List<String> tags = GitTagCalculator.create(repo.getProject()).tagsForHead(repo.getRoot());
    return RepoInfo.create(status, count, tags);
  }
}
